package org.server.reservation.record.dto;

import jakarta.validation.constraints.NotNull;
import org.server.reservation.record.entity.ReservationRecordStatus;

public record ModifyReservationRecordRequest(
        @NotNull
        ReservationRecordStatus reservationRecordStatus
) {
}
