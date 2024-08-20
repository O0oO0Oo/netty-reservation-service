package org.server.rsaga.business.dto.request;

import org.hibernate.validator.constraints.Length;
import org.server.rsaga.business.domain.constant.BusinessDetailCategory;
import org.server.rsaga.business.domain.constant.BusinessMajorCategory;
import org.server.rsaga.business.domain.constant.BusinessSubCategory;

public record ModifyBusinessRequest(
        @Length(min = 1, max = 20, message = "이름은 1 ~ 20자 입니다.")
        String name,

        BusinessMajorCategory businessMajorCategory,
        BusinessSubCategory businessSubCategory,
        BusinessDetailCategory businessDetailCategory
        ) {
}