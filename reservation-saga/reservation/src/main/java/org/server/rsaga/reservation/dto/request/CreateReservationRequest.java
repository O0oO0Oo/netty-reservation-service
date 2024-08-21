package org.server.rsaga.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;
import org.server.rsaga.common.domain.constant.PaymentType;

public record CreateReservationRequest(
        @NotNull(message = "userId 는 필수 입력 값입니다")
        Long userId,
        @NotNull(message = "businessId 는 필수 입력 값입니다")
        Long businessId,
        @NotNull(message = "reservableItemId 는 필수 입력 값입니다")
        Long reservableItemId,
        @NotNull(message = "reservableTimeId 는 필수 입력 값입니다")
        Long reservableTimeId,
        @NotNull(message = "requestQuantity 는 필수 입력 값입니다")
        @Range(min = 1, message = "구매 수량은 1 이상이어야 합니다.")
        Long requestQuantity,

        @NotNull(message = "paymentType 는 필수 입력 값입니다.")
        PaymentType paymentType
) {
}
