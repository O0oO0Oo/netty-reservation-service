package org.server.rsaga.business.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.server.rsaga.business.entity.BusinessType;

public record RegisterBusinessRequest(
        @NotNull(message = "이름은 필수 입력 값입니다")
        @Length(min = 1, max = 20, message = "이름은 1 ~ 20자 입니다.")
        String name,

        @NotNull(message = "회사 유형은 필수 입니다.")
        BusinessType businessType
        ) {
}