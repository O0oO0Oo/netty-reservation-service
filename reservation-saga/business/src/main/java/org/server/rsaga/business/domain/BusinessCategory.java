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

    public BusinessCategory(
            final BusinessMajorCategory majorCategory,
            final BusinessSubCategory subCategory,
            final BusinessDetailCategory detailCategory) {
        checkMajorCategory(majorCategory);
        this.majorCategory = majorCategory;

        this.subCategory = subCategory;

        checkDetailCategory(
                subCategory, detailCategory
        );
        this.detailCategory = detailCategory;
    }

    private void checkMajorCategory(
            final BusinessMajorCategory majorCategory
    ) {
        if (majorCategory == null) {
            throw new IllegalArgumentException("Major category cannot be empty.");
        }
    }

    private void checkDetailCategory(
            final BusinessSubCategory subCategory,
            final BusinessDetailCategory detailCategory)
    {
        if (subCategory == null && detailCategory != null) {
            throw new IllegalArgumentException("In order for DetailCategory to be set, Subcategory should not be null.");
        }
    }

    protected BusinessCategory() {
        this.majorCategory = null;
        this.subCategory = null;
        this.detailCategory = null;
    }
}