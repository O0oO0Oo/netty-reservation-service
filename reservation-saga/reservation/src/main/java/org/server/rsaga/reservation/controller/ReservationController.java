package org.server.rsaga.reservation.controller;

import io.netty.handler.codec.http.FullHttpResponse;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.dto.FullHttpResponseBuilder;
import org.server.rsaga.reservation.dto.ModifyReservationRequest;
import org.server.rsaga.reservation.dto.RegisterReservationRequest;
import org.server.rsaga.reservation.service.ReservationRecordService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationRecordService reservationRecordService;

    @GetMapping("/{user_id}/reservation")
    public FullHttpResponse findReservationRecordList(@PathVariable("user_id") Long userId) {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservationRecordService.findReservationRecordList(userId)
                )
                .build();
    }

    @GetMapping("/{user_id}/reservation/{reservation_id}")
    public FullHttpResponse findReservationRecord(@PathVariable("user_id") Long userId, @PathVariable("reservation_id") Long reservationId) {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservationRecordService.findReservationRecord(userId, reservationId)
                )
                .build();
    }

    @PostMapping("/{user_id}/reservation")
    public FullHttpResponse registerReservationRecord(@PathVariable("user_id") Long userId, @RequestBody RegisterReservationRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservationRecordService.registerReservationRecord(userId, request)
                )
                .build();
    }

    @PutMapping("/{user_id}/reservation/{reservation_id}")
    public FullHttpResponse modifyReservationRecord(@PathVariable("user_id") Long userId, @PathVariable("reservation_id") Long reservationId, @RequestBody ModifyReservationRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservationRecordService.modifyReservationRecord(userId, reservationId, request)
                )
                .build();
    }
}
