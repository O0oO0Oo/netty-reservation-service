package com.server.reservation.reservation.controller;

import com.server.reservation.common.dto.FullHttpResponseBuilder;
import com.server.reservation.reservation.dto.request.FindReservationRequest;
import com.server.reservation.reservation.dto.request.RegisterReservationRequest;
import com.server.reservation.reservation.dto.request.ModifyReservationRequest;
import com.server.reservation.reservation.service.ReservationService;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationRecordService;

    @GetMapping("/{user_id}/reservation")
    public FullHttpResponse findReservationList(@PathVariable("user_id") Long userId) {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservationRecordService.findReservationList(userId)
                )
                .build();
    }

    @GetMapping("/{reservation_id}")
    public FullHttpResponse findReservation(@PathVariable("reservation_id") Long reservationId,
                                            @RequestBody FindReservationRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservationRecordService.findReservation(request.userId(), reservationId)
                )
                .build();
    }

    @PostMapping
    public FullHttpResponse registerReservation(@RequestBody RegisterReservationRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservationRecordService.registerReservation(request)
                )
                .build();
    }

    @PutMapping("/{reservation_id}")
    public FullHttpResponse modifyReservation(@PathVariable("reservation_id") Long reservationId, @RequestBody ModifyReservationRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservationRecordService.modifyReservation(reservationId, request)
                )
                .build();
    }
}
