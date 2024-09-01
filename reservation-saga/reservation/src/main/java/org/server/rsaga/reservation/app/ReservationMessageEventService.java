package org.server.rsaga.reservation.app;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.messaging.message.ErrorDetails;
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
                        createFailureResponse(message, ErrorCode.INVALID_RESERVATION_STATUS)
                );
            }
        }

        return messageByReservationStatus;
    }

    private List<Message<String, CreateReservationEvent>> handlePendingReservations(List<Message<String, CreateReservationEvent>> messages) {
        List<Message<String, CreateReservationEvent>> responses = new ArrayList<>(messages.size());

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

        Map<UserItemPairDto, Long> reservedSumQuantityMap = findReservedSumQuantity(userItemPairs);
        LinkedList<Reservation> reservations = new LinkedList<>();

        for (Message<String, CreateReservationEvent> message : messages) {
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

            if (reservedSumQuantityMap.containsKey(pairDto)) {
                Long reservedQuantity = reservedSumQuantityMap.get(pairDto);
                if (maxQuantityPerUser < (reservedQuantity + requestQuantity)) {
                    // 인당 구매 제한 초과, 실패 응답
                    responses.add(createFailureResponse(message, ErrorCode.MAX_QUANTITY_EXCEEDED));
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
                    responses.add(
                            createCheckReservationLimitSuccessResponse(message, reservationId)
                    );
                }

            }
            else {
                // 없다면 실패 응답
                responses.add(createFailureResponse(message, ErrorCode.RESERVATION_NOT_FOUND));
            }
        }

        reservationJpaRepository.saveAll(reservations);

        return responses;
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

    private List<Message<String, CreateReservationEvent>> handleFailedReservations(List<Message<String, CreateReservationEvent>> messages) {
        List<Message<String, CreateReservationEvent>> responses = new ArrayList<>(messages.size());

        Set<Long> reservationIds = new HashSet<>();
        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            CheckReservationLimitRequestOuterClass.CheckReservationLimitRequest checkReservation = requestPayload.getCheckReservation();

            long reservationId = checkReservation.getReservationId();
            reservationIds.add(reservationId);
        }

        Map<Long, Reservation> reservationMap = reservationJpaRepository.findAllById(reservationIds).stream()
                .collect(Collectors.toMap(
                        Reservation::getId,
                        reservation -> reservation
                ));

        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            CheckReservationLimitRequestOuterClass.CheckReservationLimitRequest checkReservation = requestPayload.getCheckReservation();

            long reservationId = checkReservation.getReservationId();

            if (reservationMap.containsKey(reservationId)) {
                Reservation reservation = reservationMap.get(reservationId);
                reservation.updateStatus(ReservationStatus.FAILED);

                // 실패로 상태 변경 성공 응답.
                responses.add(createCheckReservationLimitSuccessResponse(message, reservationId));
            }
            else {
                // 없다면 실패 응답.
                responses.add(createFailureResponse(message, ErrorCode.RESERVATION_NOT_FOUND));
            }
        }

        return responses;
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

    @Transactional
    public List<Message<String, CreateReservationEvent>> consumeBulkCreateReservationFinalEvent(List<Message<String, CreateReservationEvent>> messages) {
        List<Message<String, CreateReservationEvent>> responses = new ArrayList<>(messages.size());

        Set<Long> reservationIds = new HashSet<>();
        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            CreateReservationFinalRequestOuterClass.CreateReservationFinalRequest createReservationFinal = requestPayload.getCreateReservationFinal();
            long reservationId = createReservationFinal.getReservationId();

            reservationIds.add(reservationId);
        }

        Map<Long, Reservation> reservationMap = reservationJpaRepository.findAllById(reservationIds).stream()
                .collect(Collectors.toMap(
                        Reservation::getId,
                        reservation -> reservation
                ));

        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            CreateReservationFinalRequestOuterClass.CreateReservationFinalRequest createReservationFinal = requestPayload.getCreateReservationFinal();
            long reservationId = createReservationFinal.getReservationId();

            if (reservationMap.containsKey(reservationId)) {
                Reservation reservation = reservationMap.get(reservationId);
                reservation.updateStatus(ReservationStatus.FAILED);

                // 실패로 상태 변경 성공 응답.
                responses.add(createReservationFinalSuccessResponse(message, reservation));
            }
            else {
                // 없다면 실패 응답.
                responses.add(createFailureResponse(message, ErrorCode.RESERVATION_NOT_FOUND));
            }
        }

        return responses;
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

    private Message<String, CreateReservationEvent> createFailureResponse(Message<String, CreateReservationEvent> message, ErrorCode errorCode) {
        Map<String, byte[]> metadata = message.metadata();

        metadata.put(ErrorDetails.ERROR_CODE, errorCode.getCode().getBytes());
        metadata.put(ErrorDetails.ERROR_MESSAGE, errorCode.getMessage().getBytes());

        return SagaMessage.of(message.key(), message.payload(), metadata, Message.Status.RESPONSE_FAILED);
    }
}