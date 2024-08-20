package org.server.rsaga.reservableitem.dto.request;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

public record ModifyReservableItemRequest(
        @Length(min = 1, max = 20, message = "이름은 1 ~ 20자 입니다.")
        String name,
        @Range(min = 1L, message = "수량은 1 이상이어야 합니다.")
        Long maxQuantityPerUser,
        @Range(min = 0L, message = "가격은 0원 이상이어야 합니다.")
        Long price,
        @NotNull(message = "회사 id 는 필수입니다.")
        Long businessId,

        Boolean isItemAvailable,

        ModifyReservableTime reservableTime
) {
}
