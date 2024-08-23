package org.server.rsaga.business.dto.request;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.server.rsaga.business.domain.constant.BusinessDetailCategory;
import org.server.rsaga.business.domain.constant.BusinessMajorCategory;
import org.server.rsaga.business.domain.constant.BusinessSubCategory;

public record RegisterBusinessRequest(
        @NotNull(message = "이름은 필수 입력 값입니다")
        @Length(min = 1, max = 50, message = "이름은 1 ~ 20자 입니다.")
        String name,

        @NotNull(message = "회사 유형 주요 카테고리는 필수 입니다.")
        BusinessMajorCategory businessMajorCategory,

        BusinessSubCategory businessSubCategory,

        BusinessDetailCategory businessDetailCategory
        ) {
}