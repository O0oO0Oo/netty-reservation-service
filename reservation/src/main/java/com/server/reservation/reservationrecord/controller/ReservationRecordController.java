package com.server.reservation.reservationrecord.controller;

import com.server.reservation.common.dto.FullHttpResponseBuilder;
import com.server.reservation.reservationrecord.dto.ModifyReservationRecordRequest;
import com.server.reservation.reservationrecord.dto.RegisterReservationRecordRequest;
import com.server.reservation.reservationrecord.service.ReservationRecordService;
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
