package org.server.rsaga.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.server.rsaga.business.domain.constant.BusinessDetailCategory;
import org.server.rsaga.business.domain.constant.BusinessMajorCategory;
import org.server.rsaga.business.domain.constant.BusinessSubCategory;

@Value
@Embeddable
@EqualsAndHashCode
public class BusinessCategory {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    BusinessMajorCategory majorCategory;
    @Enumerated(EnumType.STRING)
    BusinessSubCategory subCategory;
    @Enumerated(EnumType.STRING)
    BusinessDetailCategory detailCategory;

    public BusinessCategory(BusinessMajorCategory majorCategory, BusinessSubCategory subCategory, BusinessDetailCategory detailCategory) {
        if (majorCategory == null) {
            throw new IllegalArgumentException("Major category cannot be empty.");
        }
        this.majorCategory = majorCategory;
        this.subCategory = subCategory;
        this.detailCategory = detailCategory;
    }

    protected BusinessCategory() {
        this.majorCategory = null;
        this.subCategory = null;
        this.detailCategory = null;
    }
}