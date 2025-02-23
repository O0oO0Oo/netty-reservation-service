package org.server.rsaga.reservableitem.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.util.List;

public record RegisterReservableItemRequest(
        @NotEmpty(message = "이름은 필수 입력 값입니다")
        @Length(min = 1, max = 20, message = "이름은 1 ~ 20자 입니다.")
        String name,

        @NotNull
        @Range(min = 0L, message = "최대 구매 수량운 0 이상이어야 합니다.")
        Long maxQuantityPerUser,

        @NotNull
        @Range(min = 0L, message = "가격은 0원 이상이어야 합니다.")
        Long price,

        @NotNull(message = "회사 id 는 필수입니다.")
        Long businessId,

        Boolean isItemAvailable,

        @NotNull(message = "예약가능 시간 등록은 필수입니다.")
        List<RegisterReservableTime> reservableTimes
) {
}