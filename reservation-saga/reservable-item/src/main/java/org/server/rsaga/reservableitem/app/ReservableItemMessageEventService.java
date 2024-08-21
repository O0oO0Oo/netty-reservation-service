package org.server.rsaga.reservableitem.app;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.reservableitem.UpdateReservableItemQuantityRequestOuterClass;
import org.server.rsaga.reservableitem.VerifyReservableItemRequestOuterClass;
import org.server.rsaga.reservableitem.domain.ReservableItem;
import org.server.rsaga.reservableitem.infra.repository.ReservableItemCustomRepository;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.saga.api.SagaMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservableItemMessageEventService {
    private final ReservableItemCustomRepository reservableItemCustomRepository;

    @Transactional
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
        if (reservableItem.isTimeAvailable(reservableTimeId)) {
            throw new CustomException(ErrorCode.RESERVABLE_ITEM_IS_NOT_AVAILABLE);
        }
    }

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
}