package com.server.reservation.reservation.dto.request;

import jakarta.validation.constraints.NotNull;

public record FindReservationRequest(
        @NotNull(message = "userId 는 필수 입력 값입니다")
        Long userId
) {
}
