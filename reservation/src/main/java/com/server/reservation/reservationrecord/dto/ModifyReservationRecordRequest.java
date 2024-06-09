package com.server.reservation.reservationrecord.dto;

import com.server.reservation.reservationrecord.entity.ReservationRecordStatus;
import jakarta.validation.constraints.NotNull;

public record ModifyReservationRecordRequest(
        @NotNull
        ReservationRecordStatus reservationRecordStatus
) {
}
