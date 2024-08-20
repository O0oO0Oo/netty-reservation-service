package org.server.rsaga.business.dto.response;

import org.server.rsaga.business.domain.Business;
import org.server.rsaga.business.domain.BusinessCategory;
import org.server.rsaga.business.domain.constant.BusinessDetailCategory;
import org.server.rsaga.business.domain.constant.BusinessMajorCategory;
import org.server.rsaga.business.domain.constant.BusinessSubCategory;

public record BusinessDetailsResponse(
        Long businessId,
        String businessName,
        BusinessMajorCategory majorCategory,
        BusinessSubCategory subCategory,
        BusinessDetailCategory detailCategory
) {
    public static BusinessDetailsResponse of(
            Business business
    ) {
        return new BusinessDetailsResponse(
                business.getId(),
                business.getName(),
                business.getMajorCategory(),
                business.getSubCategory(),
                business.getDetailCategory()
        );
    }
}