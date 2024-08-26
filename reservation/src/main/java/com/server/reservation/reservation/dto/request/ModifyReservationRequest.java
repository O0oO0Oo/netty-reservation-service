package com.server.reservation.reservation.dto.request;

import com.server.reservation.reservation.domain.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public record ModifyReservationRequest(
        @NotNull(message = "userId 는 필수 입력 값입니다")
        Long userId,
        @NotNull(message = "reservationStatus 는 필수 입력 값입니다. (RESERVED, CANCELED, COMPLETED, PENDING, FAILED)")
        ReservationStatus reservationStatus
) {
}
