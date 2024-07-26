package org.server.reservation.record.controller;

import org.server.reservation.core.common.dto.FullHttpResponseBuilder;
import org.server.reservation.record.dto.ModifyReservationRecordRequest;
import org.server.reservation.record.dto.RegisterReservationRecordRequest;
import org.server.reservation.record.service.ReservationRecordService;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class ReservationRecordController {
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
    public FullHttpResponse registerReservationRecord(@PathVariable("user_id") Long userId, @RequestBody RegisterReservationRecordRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservationRecordService.registerReservationRecord(userId, request)
                )
                .build();
    }

    @PutMapping("/{user_id}/reservation/{reservation_id}")
    public FullHttpResponse modifyReservationRecord(@PathVariable("user_id") Long userId, @PathVariable("reservation_id") Long reservationId, @RequestBody ModifyReservationRecordRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservationRecordService.modifyReservationRecord(userId, reservationId, request)
                )
                .build();
    }
}
