package org.server.rsaga.reservableitem.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.util.Date;

public record RegisterItemRequest(
        @NotEmpty(message = "이름은 필수 입력 값입니다")
        @Length(min = 1, max = 20, message = "이름은 1 ~ 20자 입니다.")
        String name,
        @NotNull(message = "수량은 필수 입력 값입니다")
        @Range(min = 1L, message = "수량은 1 이상이어야 합니다.")
        Long quantity,
        @NotNull
        @Range(min = 1L, message = "최대 구매 수량운 1 이상이어야 합니다.")
        Long maxQuantityPerUser,
        Date reservableTime,
        @NotNull
        @Range(min = 0L, message = "가격은 0원 이상이어야 합니다.")
        Long price
) {
}