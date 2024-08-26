package com.server.reservation.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record RegisterReservationRequest(
        @NotNull(message = "userId 는 필수 입력 값입니다")
        Long userId,
        @NotNull(message = "businessId 는 필수 입력 값입니다")
        Long businessId,
        @NotNull(message = "itemId 는 필수 입력 값입니다")
        Long itemId,
        @NotNull(message = "quantity 는 필수 입력 값입니다")
        @Range(min = 1, message = "구매 수량은 1 이상이어야 합니다.")
        Long quantity
) {
}
