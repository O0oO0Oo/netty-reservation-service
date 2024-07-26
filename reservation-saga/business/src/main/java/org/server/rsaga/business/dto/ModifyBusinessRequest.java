package org.server.rsaga.business.dto;

import org.hibernate.validator.constraints.Length;
import org.server.rsaga.business.entity.BusinessType;

public record ModifyBusinessRequest(
        @Length(min = 1, max = 20, message = "이름은 1 ~ 20자 입니다.")
        String name,
        BusinessType businessType
        ) {
}