package com.server.reservation.business.dto;

import com.server.reservation.business.entity.BusinessType;
import org.hibernate.validator.constraints.Length;

public record ModifyBusinessRequest(
        @Length(min = 1, max = 20, message = "이름은 1 ~ 20자 입니다.")
        String name,
        BusinessType businessType
        ) {
}