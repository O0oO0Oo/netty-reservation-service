package org.server.rsaga.reservation.infra.api;

import io.netty.handler.codec.http.FullHttpResponse;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.dto.FullHttpResponseBuilder;
import org.server.rsaga.reservation.app.ReservationSagaService;
import org.server.rsaga.reservation.dto.request.FindReservationRequest;
import org.server.rsaga.reservation.dto.request.ModifyReservationRequest;
import org.server.rsaga.reservation.dto.request.CreateReservationRequest;
import org.server.rsaga.reservation.app.ReservationApiService;
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

    @PostMapping
    public FullHttpResponse createReservation(@RequestBody CreateReservationRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservationSagaService.createReservation(request)
                )
                .build();
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
