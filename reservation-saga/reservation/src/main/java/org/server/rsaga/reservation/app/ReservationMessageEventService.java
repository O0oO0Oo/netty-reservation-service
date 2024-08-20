package org.server.rsaga.reservation.app;

import lombok.RequiredArgsConstructor;
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
import org.server.rsaga.reservation.infra.repository.ReservationCustomRepository;
import org.server.rsaga.reservation.infra.repository.ReservationJpaRepository;
import org.server.rsaga.saga.api.SagaMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationMessageEventService {
    private final ReservationJpaRepository reservationJpaRepository;
    private final ReservationCustomRepository reservationCustomRepository;

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
                userId,
                reservableItemId
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
}