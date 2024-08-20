package org.server.rsaga.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import org.server.rsaga.reservation.domain.constant.ReservationStatus;

public record ModifyReservationRequest(
        @NotNull(message = "userId 는 필수 입력 값입니다")
        Long userId,
        @NotNull(message = "reservationStatus 는 필수 입력 값입니다. (RESERVED, CANCELED, COMPLETED, PENDING, FAILED)")
        ReservationStatus reservationStatus
) {
}