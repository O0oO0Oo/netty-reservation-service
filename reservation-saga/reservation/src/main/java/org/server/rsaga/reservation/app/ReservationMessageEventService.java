package org.server.rsaga.reservation.app;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.reservation.CheckReservationLimitRequestOuterClass;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.reservation.CreateReservationFinalRequestOuterClass;
import org.server.rsaga.reservation.ReservationStatusOuterClass;
import org.server.rsaga.reservation.domain.Reservation;
import org.server.rsaga.reservation.domain.constant.ReservationStatus;
import org.server.rsaga.reservation.dto.repository.UserItemPairDto;
import org.server.rsaga.reservation.dto.repository.UserItemReservationSumProjection;
import org.server.rsaga.reservation.infra.repository.ReservationCustomRepository;
import org.server.rsaga.reservation.infra.repository.ReservationJpaRepository;
import org.server.rsaga.saga.api.SagaMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


// todo : 코드 리팩토링 필요

// 리팩토링 필요
@Service
@RequiredArgsConstructor
public class ReservationMessageEventService {
    private final ReservationJpaRepository reservationJpaRepository;
    private final ReservationCustomRepository reservationCustomRepository;

    /**
     * {@link org.server.rsaga.reservation.CheckReservationLimitRequestOuterClass.CheckReservationLimitRequest} 단건 처리.
     */
    @Transactional
    public Message<String, CreateReservationEvent> consumeCheckReservationLimitEvent(Message<String, CreateReservationEvent> message) {
        CreateReservationEvent requestPayload = message.payload();
        ReservationStatusOuterClass.ReservationStatus status = requestPayload.getCheckReservation().getStatus();

        if (ReservationStatusOuterClass.ReservationStatus.PENDING.equals(status)) {
            return handlePendingReservation(message);
        } else if (ReservationStatusOuterClass.ReservationStatus.FAILED.equals(status)) {
            return handleFailedReservation(message);
        }
        else {
            throw new IllegalArgumentException("Invalid reservation status : " + status);
        }
    }

    private Message<String, CreateReservationEvent> handlePendingReservation(Message<String, CreateReservationEvent> message) {
        CreateReservationEvent requestPayload = message.payload();
        CheckReservationLimitRequestOuterClass.CheckReservationLimitRequest checkReservation = requestPayload.getCheckReservation();
        long reservableItemId = checkReservation.getReservableItemId();
        long reservableTimeId = checkReservation.getReservableTimeId();
        long userId = checkReservation.getUserId();
        long businessId = checkReservation.getBusinessId();
        long reservationId = checkReservation.getReservationId();

        long maxQuantityPerUser = requestPayload.getCheckReservation().getMaxQuantityPerUser();
        long requestQuantity = requestPayload.getCheckReservation().getRequestQuantity();

        // 초과하는지 검사.
        Integer reservedQuantity = reservationJpaRepository.findSumQuantityByUserIdAndReservableItemIdAndReserved(
                new ForeignKey(userId),
                new ForeignKey(reservableItemId)
        );

        if (maxQuantityPerUser < (reservedQuantity + requestQuantity)) {
            throw new CustomException(ErrorCode.MAX_QUANTITY_EXCEEDED);
        }

        Reservation reservation = new Reservation(
                reservationId,
                businessId,
                userId,
                reservableItemId,
                reservableTimeId,
                requestQuantity
        );
        reservationJpaRepository.save(reservation);

        String key = String.valueOf(reservationId);
        CreateReservationEvent responsePayload = CreateReservationEventBuilder
                .builder()
                .setCheckReservationLimitResponse(
                        reservationId
                )
                .build();
        return SagaMessage.of(key, responsePayload, message.metadata(), Message.Status.RESPONSE_SUCCESS);
    }

    private Message<String, CreateReservationEvent> handleFailedReservation(Message<String, CreateReservationEvent> message) {
        CreateReservationEvent requestPayload = message.payload();
        CheckReservationLimitRequestOuterClass.CheckReservationLimitRequest checkReservation = requestPayload.getCheckReservation();

        long reservationId = checkReservation.getReservationId();

        Reservation reservation = reservationCustomRepository.findReservationByIdOrElseThrow(reservationId);
        reservation.updateStatus(
                ReservationStatus.FAILED
        );

        String key = String.valueOf(reservationId);
        CreateReservationEvent responsePayload = CreateReservationEventBuilder
                .builder()
                .setCheckReservationLimitResponse(
                    reservationId
                )
                .build();
        return SagaMessage.of(key, responsePayload, message.metadata(), Message.Status.RESPONSE_SUCCESS);
    }

    /**
     * {@link org.server.rsaga.reservation.CheckReservationLimitRequestOuterClass.CheckReservationLimitRequest} 벌크 처리.
     */
    @Transactional
    public List<Message<String, CreateReservationEvent>> consumeBulkCheckReservationLimitEvent(List<Message<String, CreateReservationEvent>> messages) {
        List<Message<String, CreateReservationEvent>> responses = new ArrayList<>(messages.size());

        Map<ReservationStatusOuterClass.ReservationStatus, List<Message<String, CreateReservationEvent>>> reservationStatusMap = classifyMessageByReservationStatus(messages, responses);

        // PENDING 상태의 예약 처리, 리스트가 null이 아닌 경우에만 처리
        List<Message<String, CreateReservationEvent>> pendingReservations = reservationStatusMap.get(ReservationStatusOuterClass.ReservationStatus.PENDING);
        if (pendingReservations != null) {
            responses.addAll(handlePendingReservations(pendingReservations));
        }

        // FAILED 상태의 예약 처리, 리스트가 null이 아닌 경우에만 처리
        List<Message<String, CreateReservationEvent>> failedReservations = reservationStatusMap.get(ReservationStatusOuterClass.ReservationStatus.FAILED);
        if (failedReservations != null) {
            responses.addAll(handleFailedReservations(failedReservations));
        }

        return responses;
    }

    /**
     * 메시지 예약 타입별로 분류 
     */
    private Map<ReservationStatusOuterClass.ReservationStatus, List<Message<String, CreateReservationEvent>>> classifyMessageByReservationStatus(
            List<Message<String, CreateReservationEvent>> messages,
            List<Message<String, CreateReservationEvent>> responses
    ) {
        Map<ReservationStatusOuterClass.ReservationStatus, List<Message<String, CreateReservationEvent>>> messageByReservationStatus = new EnumMap<>(ReservationStatusOuterClass.ReservationStatus.class);

        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            ReservationStatusOuterClass.ReservationStatus status = requestPayload.getCheckReservation().getStatus();

            messageByReservationStatus.putIfAbsent(status, new LinkedList<>());
            if (ReservationStatusOuterClass.ReservationStatus.PENDING.equals(status)) {
                messageByReservationStatus.get(status).add(message);
            } else if (ReservationStatusOuterClass.ReservationStatus.FAILED.equals(status)) {
                messageByReservationStatus.get(status).add(message);
            }
            else {
                responses.add(
                        SagaMessage.createFailureResponse(message, ErrorCode.INVALID_RESERVATION_STATUS)
                );
            }
        }

        return messageByReservationStatus;
    }

    /**
     * PENDING 상태 예약 처리
     */
    private List<Message<String, CreateReservationEvent>> handlePendingReservations(List<Message<String, CreateReservationEvent>> messages) {
        List<Message<String, CreateReservationEvent>> responses = new ArrayList<>(messages.size());

        Set<UserItemPairDto> userItemPairs = extractUserItemPairDtoFromMessage(messages);

        Map<UserItemPairDto, Long> reservedSumQuantityMap = findReservedSumQuantity(userItemPairs);

        List<Reservation> reservations = new LinkedList<>();
        for (Message<String, CreateReservationEvent> message : messages) {
            responses.add(
                    processCheckReservationLimitRequestMessage(message, reservedSumQuantityMap, reservations)
            );
        }

        reservationJpaRepository.saveAll(reservations);

        return responses;
    }

    /**
     * 메시지에서 검색을 위한 Dto 추출
     */
    private Set<UserItemPairDto> extractUserItemPairDtoFromMessage(List<Message<String, CreateReservationEvent>> messages) {
        Set<UserItemPairDto> userItemPairs = new HashSet<>();
        
        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            CheckReservationLimitRequestOuterClass.CheckReservationLimitRequest checkReservation = requestPayload.getCheckReservation();
            long reservableItemId = checkReservation.getReservableItemId();
            long userId = checkReservation.getUserId();

            userItemPairs.add(
                    new UserItemPairDto(
                            new ForeignKey(userId),
                            new ForeignKey(reservableItemId)
                    )
            );
        }

        return userItemPairs;
    }

    private Map<UserItemPairDto, Long> findReservedSumQuantity(Set<UserItemPairDto> userItemPairs) {
        List<UserItemReservationSumProjection> userItemReservationSumProjections = reservationCustomRepository.findSumQuantityByUserIdAndReservableItemIdIn(userItemPairs);

        return userItemReservationSumProjections.stream()
                .collect(Collectors.toMap(
                        result -> new UserItemPairDto(
                                result.userId(),
                                result.reservableItemId()
                        ),
                        UserItemReservationSumProjection::sumQuantity
                ));
    }

    private Message<String, CreateReservationEvent> processCheckReservationLimitRequestMessage(
            Message<String, CreateReservationEvent> message,
            Map<UserItemPairDto, Long> reservedSumQuantityMap,
            List<Reservation> reservations
    ) {
        CreateReservationEvent requestPayload = message.payload();
        CheckReservationLimitRequestOuterClass.CheckReservationLimitRequest checkReservation = requestPayload.getCheckReservation();
        long reservableItemId = checkReservation.getReservableItemId();
        long reservableTimeId = checkReservation.getReservableTimeId();
        long userId = checkReservation.getUserId();
        long businessId = checkReservation.getBusinessId();
        long reservationId = checkReservation.getReservationId();

        long maxQuantityPerUser = requestPayload.getCheckReservation().getMaxQuantityPerUser();
        long requestQuantity = requestPayload.getCheckReservation().getRequestQuantity();

        UserItemPairDto pairDto = new UserItemPairDto(
                new ForeignKey(userId),
                new ForeignKey(reservableItemId)
        );

        // 없다면 실패 응답
        if (!reservedSumQuantityMap.containsKey(pairDto)) {
            return SagaMessage.createFailureResponse(message, ErrorCode.RESERVATION_NOT_FOUND);
        }

        Long reservedQuantity = reservedSumQuantityMap.get(pairDto);
        if (maxQuantityPerUser < (reservedQuantity + requestQuantity)) {
            // 인당 구매 제한 초과, 실패 응답
            return SagaMessage.createFailureResponse(message, ErrorCode.MAX_QUANTITY_EXCEEDED);
        }
        else {
            // PENDING 상태의 예약 저장
            Reservation reservation = new Reservation(
                    reservationId,
                    businessId,
                    userId,
                    reservableItemId,
                    reservableTimeId,
                    requestQuantity
            );
            reservations.add(reservation);

            // 성공 응답
            return createCheckReservationLimitSuccessResponse(message, reservationId);
        }
    }

    private List<Message<String, CreateReservationEvent>> handleFailedReservations(List<Message<String, CreateReservationEvent>> messages) {
        List<Message<String, CreateReservationEvent>> responses = new ArrayList<>(messages.size());

        Set<Long> reservationIds = extractReservationIdFromCheckReservationLimitRequestMessage(messages);

        Map<Long, Reservation> reservationMap = findReservationMapByIds(reservationIds);

        for (Message<String, CreateReservationEvent> message : messages) {
            responses.add(
                    processCheckReservationLimitRequestFailedMessage(message, reservationMap)
            );
        }

        return responses;
    }

    /**
     * 메시지에서 reservation id 추출
     */
    private Set<Long> extractReservationIdFromCheckReservationLimitRequestMessage(List<Message<String, CreateReservationEvent>> messages) {
        Set<Long> reservationIds = new HashSet<>();
        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            CheckReservationLimitRequestOuterClass.CheckReservationLimitRequest checkReservation = requestPayload.getCheckReservation();

            long reservationId = checkReservation.getReservationId();
            reservationIds.add(reservationId);
        }

        return reservationIds;
    }

    private Map<Long, Reservation> findReservationMapByIds(Set<Long> reservationIds) {
        return reservationJpaRepository.findAllById(reservationIds).stream()
                .collect(Collectors.toMap(
                                Reservation::getId,
                                reservation -> reservation
                        )
                );
    }

    private Message<String, CreateReservationEvent> processCheckReservationLimitRequestFailedMessage(
            Message<String, CreateReservationEvent> message,
            Map<Long, Reservation> reservationMap
    ) {
        CreateReservationEvent requestPayload = message.payload();
        CheckReservationLimitRequestOuterClass.CheckReservationLimitRequest checkReservation = requestPayload.getCheckReservation();

        long reservationId = checkReservation.getReservationId();

        // 없다면 실패 메시지
        if (!reservationMap.containsKey(reservationId)) {
            return SagaMessage.createFailureResponse(message, ErrorCode.RESERVATION_NOT_FOUND);
        }
        
        Reservation reservation = reservationMap.get(reservationId);
        reservation.updateStatus(ReservationStatus.FAILED);

        // 실패로 상태 변경 성공 응답.
        return createCheckReservationLimitSuccessResponse(message, reservationId);
    }

    private Message<String, CreateReservationEvent> createCheckReservationLimitSuccessResponse(Message<String, CreateReservationEvent> message, Long reservationId) {
        String key = String.valueOf(reservationId);
        CreateReservationEvent responsePayload = CreateReservationEventBuilder
                .builder()
                .setCheckReservationLimitResponse(
                        reservationId
                )
                .build();
        return SagaMessage.of(key, responsePayload, message.metadata(), Message.Status.RESPONSE_SUCCESS);
    }

    /**
     * {@link org.server.rsaga.reservation.CreateReservationFinalRequestOuterClass.CreateReservationFinalRequest} 단건 처리 
     */
    @Transactional
    public Message<String, CreateReservationEvent> consumeCreateReservationFinalEvent(Message<String, CreateReservationEvent> message) {
        CreateReservationEvent requestPayload = message.payload();
        CreateReservationFinalRequestOuterClass.CreateReservationFinalRequest createReservationFinal = requestPayload.getCreateReservationFinal();
        long reservationId = createReservationFinal.getReservationId();

        Reservation reservation = reservationCustomRepository.findReservationByIdOrElseThrow(
                reservationId
        );

        reservation.updateStatus(ReservationStatus.RESERVED);

        String key = String.valueOf(reservationId);
        CreateReservationEvent responsePayload = CreateReservationEventBuilder
                .builder()
                .setRegisterReservationFinalResponse(
                        reservation.getId(),
                        reservation.getBusinessId(),
                        reservation.getUserId(),
                        reservation.getReservableItemId(),
                        reservation.getReservableTimeId(),
                        reservation.getQuantity()
                )
                .build();
        return SagaMessage.of(key, responsePayload, message.metadata(), Message.Status.RESPONSE_SUCCESS);
    }

    /**
     * {@link org.server.rsaga.reservation.CreateReservationFinalRequestOuterClass.CreateReservationFinalRequest} 벌크 처리 
     */
    @Transactional
    public List<Message<String, CreateReservationEvent>> consumeBulkCreateReservationFinalEvent(List<Message<String, CreateReservationEvent>> messages) {
        List<Message<String, CreateReservationEvent>> responses = new ArrayList<>(messages.size());

        Set<Long> reservationIds = extractReservationIdFromCreateReservationFinalRequestMessage(messages);

        Map<Long, Reservation> reservationMap = findReservationMapByIds(reservationIds);

        for (Message<String, CreateReservationEvent> message : messages) {
            responses.add(
                    processCreateReservationFinalRequest(message, reservationMap)
            );
        }

        return responses;
    }

    private Set<Long> extractReservationIdFromCreateReservationFinalRequestMessage(List<Message<String, CreateReservationEvent>> messages) {
        Set<Long> reservationIds = new HashSet<>();
        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            CreateReservationFinalRequestOuterClass.CreateReservationFinalRequest createReservationFinal = requestPayload.getCreateReservationFinal();
            long reservationId = createReservationFinal.getReservationId();

            reservationIds.add(reservationId);
        }

        return reservationIds;
    }

    private Message<String, CreateReservationEvent> processCreateReservationFinalRequest(
            Message<String, CreateReservationEvent> message,
            Map<Long, Reservation> reservationMap
    ) {
        CreateReservationEvent requestPayload = message.payload();
        CreateReservationFinalRequestOuterClass.CreateReservationFinalRequest createReservationFinal = requestPayload.getCreateReservationFinal();
        long reservationId = createReservationFinal.getReservationId();

        // 없다면 실패 메시지
        if (!reservationMap.containsKey(reservationId)) {
            return SagaMessage.createFailureResponse(message, ErrorCode.RESERVATION_NOT_FOUND);
        }

        Reservation reservation = reservationMap.get(reservationId);
        reservation.updateStatus(ReservationStatus.RESERVED);

        // PENDING 상태에서 RESERVED 상태로 변경 성공, 성공 응답.
        return createReservationFinalSuccessResponse(message, reservation);
    }

    private Message<String, CreateReservationEvent> createReservationFinalSuccessResponse(Message<String, CreateReservationEvent> message, Reservation reservation) {
        String key = String.valueOf(reservation.getId());
        CreateReservationEvent responsePayload = CreateReservationEventBuilder
                .builder()
                .setRegisterReservationFinalResponse(
                        reservation.getId(),
                        reservation.getBusinessId(),
                        reservation.getUserId(),
                        reservation.getReservableItemId(),
                        reservation.getReservableTimeId(),
                        reservation.getQuantity()
                )
                .build();
        return SagaMessage.of(key, responsePayload, message.metadata(), Message.Status.RESPONSE_SUCCESS);
    }
}