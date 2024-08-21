package org.server.rsaga.reservation.app;

import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.reservation.CreateReservationFinalResponseOuterClass;
import org.server.rsaga.reservation.dto.request.CreateReservationRequest;
import org.server.rsaga.reservation.dto.response.ReservationDetailsResponse;
import org.server.rsaga.saga.api.SagaCoordinator;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.util.SagaMessageUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationSagaService {
    private final SagaCoordinator<String, CreateReservationEvent> sagaCoordinator;

    public ReservationDetailsResponse createReservation(CreateReservationRequest request) {
        TSID correlationId = TSID.fast();

        // TSID key
        String key = correlationId.toString();

        CreateReservationEvent createReservationEvent = CreateReservationEventBuilder
                .builder()
                .setRegisterReservationInitRequest(
                        request.paymentType().name(),
                        request.userId(),
                        request.businessId(),
                        request.reservableItemId(),
                        request.reservableTimeId(),
                        request.requestQuantity()
                )
                .build();

        Map<String, byte[]> metadata = new HashMap<>();
        metadata.put(SagaMessage.STEP_ID, SagaMessageUtil.intToByteArray(0));
        metadata.put(SagaMessage.CORRELATION_ID, SagaMessageUtil.tsidToBytes(correlationId));

        SagaMessage<String, CreateReservationEvent> sagaMessage = SagaMessage.of(key, createReservationEvent, metadata, Message.Status.REQUEST);

        SagaMessage<String, CreateReservationEvent> result = sagaCoordinator.start(sagaMessage);
        CreateReservationEvent payload = result.payload();
        CreateReservationFinalResponseOuterClass.CreateReservationFinalResponse createdReservationFinal = payload.getCreatedReservationFinal();
        return ReservationDetailsResponse.of(createdReservationFinal);
    }
}
