package org.server.rsaga.reservableitem.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.reservableitem.UpdateReservableItemQuantityRequestOuterClass;
import org.server.rsaga.reservableitem.VerifyReservableItemRequestOuterClass;
import org.server.rsaga.reservableitem.domain.ReservableItem;
import org.server.rsaga.reservableitem.dto.repository.ReservableItemQueryDto;
import org.server.rsaga.reservableitem.dto.repository.ReservableItemSearchDTO;
import org.server.rsaga.reservableitem.infra.repository.ReservableItemCustomRepository;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.saga.api.SagaMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservableItemMessageEventService {
    private final ReservableItemCustomRepository reservableItemCustomRepository;

    /**
     * {@link org.server.rsaga.reservableitem.VerifyReservableItemRequestOuterClass.VerifyReservableItemRequest} 를 하나씩 처리
     */
    @Transactional(readOnly = true)
    public Message<String, CreateReservationEvent> consumeVerifyReservableItemEvent(Message<String, CreateReservationEvent> message) {
        CreateReservationEvent requestPayload = message.payload();
        VerifyReservableItemRequestOuterClass.VerifyReservableItemRequest verifyReservableItem = requestPayload.getVerifyReservableItem();
        long reservableItemId = verifyReservableItem.getReservableItemId();
        long reservableTimeId = verifyReservableItem.getReservableTimeId();
        long requestQuantity = verifyReservableItem.getRequestQuantity();
        long businessId = verifyReservableItem.getBusinessId();

        ReservableItem reservableItem = reservableItemCustomRepository.findByIdAndBusinessIdAndReservableTimeIdOrElseThrow(
                reservableItemId,
                new ForeignKey(businessId),
                reservableTimeId
        );

        // 현재 사용 가능한지
        validateItemAvailability(reservableItem, reservableTimeId);

        // 인당 최대 구매제한 넘는지
        reservableItem.validateRequestQuantity(requestQuantity);

        String key = String.valueOf(reservableItem.getId());
        CreateReservationEvent responsePayload = CreateReservationEventBuilder
                .builder()
                .setVerifyReservableItemResponse(
                        reservableItem.getId(),
                        reservableTimeId,
                        reservableItem.getPrice(),
                        reservableItem.getMaxQuantityPerUser()
                )
                .build();
        return SagaMessage.of(key, responsePayload, message.metadata(), Message.Status.RESPONSE_SUCCESS);
    }

    private void validateItemAvailability(ReservableItem reservableItem, long reservableTimeId) {
        if (!reservableItem.isTimeAvailable(reservableTimeId)) {
            throw new CustomException(ErrorCode.RESERVABLE_ITEM_IS_NOT_AVAILABLE);
        }
    }

    /**
     * {@link org.server.rsaga.reservableitem.VerifyReservableItemRequestOuterClass.VerifyReservableItemRequest} 를 모아서 처리
     */
    @Transactional(readOnly = true)
    public List<Message<String, CreateReservationEvent>> consumeBulkVerifyReservableItemEvent(List<Message<String, CreateReservationEvent>> messages) {
        Set<ReservableItemSearchDTO> queryDTOs = extractSearchDtosFromMessages(messages);

        Map<ReservableItemSearchDTO, ReservableItem> reservableItemMap = findReservableItems(queryDTOs);
        return generateResponseMessages(messages, reservableItemMap);
    }

    /**
     * Repository 에서 검색을 위한 Dto 로 변환
     */
    private Set<ReservableItemSearchDTO> extractSearchDtosFromMessages(
            List<Message<String, CreateReservationEvent>> messages) {
        Set<ReservableItemSearchDTO> ids = new HashSet<>(messages.size());

        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            VerifyReservableItemRequestOuterClass.VerifyReservableItemRequest verifyReservableItem = requestPayload.getVerifyReservableItem();
            long reservableItemId = verifyReservableItem.getReservableItemId();
            long reservableTimeId = verifyReservableItem.getReservableTimeId();
            long businessId = verifyReservableItem.getBusinessId();

            ids.add(
                    new ReservableItemSearchDTO(
                            reservableItemId,
                            new ForeignKey(businessId),
                            reservableTimeId
                    )
            );
        }
        return ids;
    }

    /**
     * 찾은 ReservableItem 을 Dto 와 매핑, 요청한 이벤트 메시지에 맞는 결과가 받아졌는지 확인하기 위해.
     */
    private Map<ReservableItemSearchDTO, ReservableItem> findReservableItems(Set<ReservableItemSearchDTO> queryDTOs) {
        List<ReservableItem> reservableItemList = reservableItemCustomRepository.findExactMatchReservableItemsWithTimesBatch(queryDTOs);

        return reservableItemList.stream()
                .collect(Collectors.toMap(
                        item -> new ReservableItemSearchDTO(
                                item.getId(),
                                new ForeignKey(item.getBusinessId()),
                                item.getReservableTimes().get(0).getId()
                        ),
                        item -> item
                ));
    }

    // 응답 메시지 생성
    private List<Message<String, CreateReservationEvent>> generateResponseMessages( List<Message<String, CreateReservationEvent>> messages,
                                                                                    Map<ReservableItemSearchDTO, ReservableItem> reservableItemMap) {
        List<Message<String, CreateReservationEvent>> responses = new ArrayList<>(messages.size());

        for (Message<String, CreateReservationEvent> message : messages) {
            Message<String, CreateReservationEvent> responseMessage = processVerifyReservableItemRequestMessage(message, reservableItemMap);
            responses.add(responseMessage);
        }
        return responses;
    }


    /**
     * 응답할 메시지를 생성한다.
     */
    private Message<String, CreateReservationEvent> processVerifyReservableItemRequestMessage(
            Message<String, CreateReservationEvent> message,
            Map<ReservableItemSearchDTO, ReservableItem> reservableItemMap) {

        CreateReservationEvent requestPayload = message.payload();
        VerifyReservableItemRequestOuterClass.VerifyReservableItemRequest request = requestPayload.getVerifyReservableItem();

        ReservableItemSearchDTO queryDTO = new ReservableItemSearchDTO(
                request.getReservableItemId(),
                new ForeignKey(request.getBusinessId()),
                request.getReservableTimeId()
        );
        
        // 없다면 실패 응답
        if (!reservableItemMap.containsKey(queryDTO)) {
            return SagaMessage.createFailureResponse(message, ErrorCode.RESERVABLE_ITEM_NOT_FOUND);
        }
        
        return createSuccessResponse(message, reservableItemMap.get(queryDTO), request);
    }

    private Message<String, CreateReservationEvent> createSuccessResponse(
            Message<String, CreateReservationEvent> message,
            ReservableItem reservableItem,
            VerifyReservableItemRequestOuterClass.VerifyReservableItemRequest request) {

        String key = String.valueOf(reservableItem.getId());
        CreateReservationEvent responsePayload = CreateReservationEventBuilder
                .builder()
                .setVerifyReservableItemResponse(
                        reservableItem.getId(),
                        request.getReservableTimeId(),
                        reservableItem.getPrice(),
                        reservableItem.getMaxQuantityPerUser()
                )
                .build();

        // 아이템이 이용 가능한지
        if (!reservableItem.isTimeAvailable(request.getReservableTimeId())){
            return SagaMessage.createFailureResponse(message, ErrorCode.RESERVABLE_ITEM_IS_NOT_AVAILABLE);
        }

        // 요청한 갯수가 인당 구매제한 보다 적은지 확인
        else if(!reservableItem.checkRequestQuantityLowerThenLimit(request.getRequestQuantity())) {
            return SagaMessage.createFailureResponse(message, ErrorCode.EXCEED_PURCHASE_LIMIT);
        } else {
            return SagaMessage.of(key, responsePayload, message.metadata(), Message.Status.RESPONSE_SUCCESS);

        }
    }

    /**
     * {@link org.server.rsaga.reservableitem.UpdateReservableItemQuantityRequestOuterClass.UpdateReservableItemQuantityRequest} 단건 처리
     */
    @Transactional
    public Message<String, CreateReservationEvent> consumeUpdateReservableItemQuantityEvent(Message<String, CreateReservationEvent> message) {
        CreateReservationEvent requestPayload = message.payload();
        UpdateReservableItemQuantityRequestOuterClass.UpdateReservableItemQuantityRequest updateReservableItemQuantity = requestPayload.getUpdateReservableItemQuantity();
        long reservableItemId = updateReservableItemQuantity.getReservableItemId();
        long reservableTimeId = updateReservableItemQuantity.getReservableTimeId();
        long requestQuantity = updateReservableItemQuantity.getRequestQuantity();

        ReservableItem reservableItem = reservableItemCustomRepository.findByIdAndReservableTimeIdOrElseThrow(
                reservableItemId,
                reservableTimeId
        );

        if (requestQuantity < 0) {
            reservableItem.decreaseReservableTimeStock(
                    reservableTimeId, -requestQuantity
            );
        }
        else {
            reservableItem.increaseReservableTimeStock(
                    reservableTimeId, requestQuantity
            );
        }

        String key = String.valueOf(reservableItemId);
        CreateReservationEvent responsePayload = CreateReservationEventBuilder
                .builder()
                .setUpdateReservableItemQuantityResponse(reservableItemId)
                .build();
        return SagaMessage.of(key, responsePayload, message.metadata(), Message.Status.RESPONSE_SUCCESS);
    }


    /**
     * {@link org.server.rsaga.reservableitem.UpdateReservableItemQuantityRequestOuterClass.UpdateReservableItemQuantityRequest} 벌크 처리
     */
    @Transactional
    public List<Message<String, CreateReservationEvent>> consumeBulkUpdateReservableItemQuantityEvent(List<Message<String, CreateReservationEvent>> messages) {
        List<Message<String, CreateReservationEvent>> responses = new ArrayList<>(messages.size());

        Set<ReservableItemQueryDto> queryDtos = extractQueryDtosFromMessages(messages);
        Map<Long, ReservableItem> reservableItemMapById = findReservableItemsByQueryDtos(queryDtos);

        for (Message<String, CreateReservationEvent> message : messages) {
            responses.add(
                    processUpdateReservableItemQuantityRequestMessage(message, reservableItemMapById)
            );
        }

        return responses;
    }

    /**
     * message 에서 검색을 위한 QueryDto 추출 
     */
    private Set<ReservableItemQueryDto> extractQueryDtosFromMessages(List<Message<String, CreateReservationEvent>> messages) {
        Set<ReservableItemQueryDto> queryDtos = new HashSet<>();

        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            UpdateReservableItemQuantityRequestOuterClass.UpdateReservableItemQuantityRequest updateReservableItemQuantity = requestPayload.getUpdateReservableItemQuantity();
            long reservableItemId = updateReservableItemQuantity.getReservableItemId();
            long reservableTimeId = updateReservableItemQuantity.getReservableTimeId();

            queryDtos.add(new ReservableItemQueryDto(reservableItemId, reservableTimeId));
        }

        return queryDtos;
    }

    private Map<Long, ReservableItem> findReservableItemsByQueryDtos(Set<ReservableItemQueryDto> queryDtos) {
        return reservableItemCustomRepository.findByIdAndReservableTimeWithBatch(queryDtos).stream()
                .collect(Collectors.toMap(ReservableItem::getId, reservableItem -> reservableItem));
    }

    private Message<String, CreateReservationEvent> processUpdateReservableItemQuantityRequestMessage(
            Message<String, CreateReservationEvent> message,
            Map<Long, ReservableItem> reservableItemMapById
    ) {
        CreateReservationEvent requestPayload = message.payload();
        UpdateReservableItemQuantityRequestOuterClass.UpdateReservableItemQuantityRequest updateReservableItemQuantity = requestPayload.getUpdateReservableItemQuantity();
        long reservableItemId = updateReservableItemQuantity.getReservableItemId();
        long reservableTimeId = updateReservableItemQuantity.getReservableTimeId();
        long requestQuantity = updateReservableItemQuantity.getRequestQuantity();

        // 아이템이 없다면 실패 응답
        if (!reservableItemMapById.containsKey(reservableItemId)) {
            return SagaMessage.createFailureResponse(message, ErrorCode.RESERVABLE_ITEM_NOT_FOUND);
        }

        ReservableItem reservableItem = reservableItemMapById.get(reservableItemId);

        // 사용 할 수 없는 시간, 아이템이라면 실패 응답
        if (!reservableItem.isTimeAvailable(reservableTimeId)) {
            return SagaMessage.createFailureResponse(message, ErrorCode.RESERVABLE_ITEM_IS_NOT_AVAILABLE);
        }

        return (requestQuantity < 0) ?
                handleDecreaseQuantityMessageEvent(message, reservableItem) :
                handleIncreaseQuantityMessageEvent(message, reservableItem);
    }

    /**
     * 재고 감소 
     */
    private Message<String, CreateReservationEvent> handleDecreaseQuantityMessageEvent(
            Message<String, CreateReservationEvent> message,
            ReservableItem reservableItem
    ) {
        CreateReservationEvent requestPayload = message.payload();
        UpdateReservableItemQuantityRequestOuterClass.UpdateReservableItemQuantityRequest updateReservableItemQuantity = requestPayload.getUpdateReservableItemQuantity();
        long reservableTimeId = updateReservableItemQuantity.getReservableTimeId();
        long requestQuantity = updateReservableItemQuantity.getRequestQuantity();

        try {
            reservableItem.decreaseReservableTimeStock(
                    reservableTimeId, -requestQuantity
            );
            return createSuccessResponse(message, reservableItem.getId());
        } catch (CustomException e) {
            return SagaMessage.createFailureResponse(message, e.getErrorCode());
        }
    }

    /**
     * 재고 복구
     */
    private Message<String, CreateReservationEvent> handleIncreaseQuantityMessageEvent(
            Message<String, CreateReservationEvent> message,
            ReservableItem reservableItem
    ){
        CreateReservationEvent requestPayload = message.payload();
        UpdateReservableItemQuantityRequestOuterClass.UpdateReservableItemQuantityRequest updateReservableItemQuantity = requestPayload.getUpdateReservableItemQuantity();
        long reservableTimeId = updateReservableItemQuantity.getReservableTimeId();
        long requestQuantity = updateReservableItemQuantity.getRequestQuantity();

        try {
            reservableItem.increaseReservableTimeStock(
                    reservableTimeId, requestQuantity
            );
            return createSuccessResponse(message, reservableItem.getId());
        } catch (CustomException e) {
            return SagaMessage.createFailureResponse(message, e.getErrorCode());
        }
    }

    private Message<String, CreateReservationEvent> createSuccessResponse(Message<String, CreateReservationEvent> message, Long reservableItemId) {
        String key = String.valueOf(reservableItemId);
        CreateReservationEvent responsePayload = CreateReservationEventBuilder
                .builder()
                .setUpdateReservableItemQuantityResponse(reservableItemId)
                .build();
        return SagaMessage.of(key, responsePayload, message.metadata(), Message.Status.RESPONSE_SUCCESS);
    }
}