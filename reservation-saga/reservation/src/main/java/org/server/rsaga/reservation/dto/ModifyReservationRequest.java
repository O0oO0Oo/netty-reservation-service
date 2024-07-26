package org.server.rsaga.reservation.dto;

import jakarta.validation.constraints.NotNull;
import org.server.rsaga.reservation.entity.ReservationStatus;

public record ModifyReservationRequest(
        @NotNull
        ReservationStatus reservationStatus
) {
}
