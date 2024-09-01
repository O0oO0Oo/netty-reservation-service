package org.server.rsaga.reservation.infra.api;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.concurrent.Promise;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.annotation.AsyncResponse;
import org.server.rsaga.common.dto.FullHttpAsyncResponse;
import org.server.rsaga.common.dto.FullHttpAsyncResponseBuilder;
import org.server.rsaga.common.dto.FullHttpResponseBuilder;
import org.server.rsaga.reservation.app.ReservationApiService;
import org.server.rsaga.reservation.app.ReservationSagaService;
import org.server.rsaga.reservation.dto.request.CreateReservationRequest;
import org.server.rsaga.reservation.dto.request.FindReservationRequest;
import org.server.rsaga.reservation.dto.request.ModifyReservationRequest;
import org.server.rsaga.reservation.dto.response.ReservationDetailsResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationApiService reservationApiService;
    private final ReservationSagaService reservationSagaService;

    @GetMapping
    public FullHttpResponse findReservationList(@RequestBody FindReservationRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservationApiService.findReservationList(request)
                )
                .build();
    }

    @GetMapping("/{reservation_id}")
    public FullHttpResponse findReservation(@RequestBody FindReservationRequest request, @PathVariable("reservation_id") Long reservationId) {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservationApiService.findReservation(request, reservationId)
                )
                .build();
    }

    @AsyncResponse
    @PostMapping
    public FullHttpAsyncResponse createReservation(@RequestBody CreateReservationRequest request) {
        Promise<ReservationDetailsResponse> reservation = reservationSagaService.createReservation(request);
        return FullHttpAsyncResponseBuilder
                .builder(
                        reservation
                ).build();
    }

    // todo : 사가 패턴을 사용할지, ApplicationEvent 로 할지
    @PutMapping("/{reservation_id}")
    public FullHttpResponse modifyReservation(@PathVariable("reservation_id") Long reservationId, @RequestBody ModifyReservationRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        null
                )
                .build();
    }
}
