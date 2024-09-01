package org.server.rsaga.reservation.app;

import io.hypersistence.tsid.TSID;
import io.netty.util.concurrent.Promise;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.netty.SharedResponseEventExecutorGroup;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.reservation.CreateReservationEvent;
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
    private final SharedResponseEventExecutorGroup sharedResponseEventExecutorGroup;
    private final SagaCoordinator<String, CreateReservationEvent> sagaCoordinator;

    public Promise<ReservationDetailsResponse> createReservation(CreateReservationRequest request) {
        TSID correlationId = TSID.fast();

        // TSID key
        String key = correlationId.toString();

        // init payload
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

        // metadata - step id , correlation id
        Map<String, byte[]> metadata = new HashMap<>();
        metadata.put(SagaMessage.STEP_ID, SagaMessageUtil.intToByteArray(0));
        metadata.put(SagaMessage.CORRELATION_ID, SagaMessageUtil.tsidToBytes(correlationId));

        // saga message
        SagaMessage<String, CreateReservationEvent> sagaMessage = SagaMessage.of(key, createReservationEvent, metadata, Message.Status.REQUEST);

        // result
        Promise<SagaMessage<String, CreateReservationEvent>> resultPromise = sagaCoordinator.start(sagaMessage);
        return convertPromise(resultPromise);
    }

    /**
     * <pre>
     * 사가 패턴의 비동기 결과인 {@link SagaMessage} 는 복잡한 Protobuf 이며,
     * 직렬화 시 순환 참조로 인해 'Direct self-reference leading to cycle (through reference chain: ?????)' 같은 예외가 발생한다.
     * Reservation 도메인의 {@link ReservationDetailsResponse} 로 응답해야 하며, 이로 인해 Promise 를 변환해서 반환해야 한다.
     * </pre>
     */
    private Promise<ReservationDetailsResponse> convertPromise(Promise<SagaMessage<String, CreateReservationEvent>> resultPromise) {
        Promise<ReservationDetailsResponse> responsePromise = sharedResponseEventExecutorGroup.getPromise();

        resultPromise
                .addListener(
                        result -> {
                            if (result.isSuccess()) {
                                SagaMessage<String, CreateReservationEvent> sagaMessage = (SagaMessage<String, CreateReservationEvent>) result.get();
                                CreateReservationEvent payload = sagaMessage.payload();

                                // 변환하여 응답.
                                ReservationDetailsResponse response = ReservationDetailsResponse.of(payload.getCreatedReservationFinal());
                                responsePromise.setSuccess(response);
                            } else {
                                // 에러 발생 리턴
                                responsePromise.setFailure(result.cause());
                            }
                        }
                );

        return responsePromise;
    }
}
