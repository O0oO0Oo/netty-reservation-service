package org.server.rsaga.business.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.server.rsaga.business.domain.constant.BusinessDetailCategory;
import org.server.rsaga.business.domain.constant.BusinessMajorCategory;
import org.server.rsaga.business.domain.constant.BusinessSubCategory;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Business domain tests")
class BusinessTest {

    @Test
    @DisplayName("Business - create - failure")
    void should_failure_when_createInvalidValueBusiness() {
        // given
        BusinessCategory businessCategory = new BusinessCategory(
                BusinessMajorCategory.RESTAURANT,
                BusinessSubCategory.CASUAL_DINING,
                BusinessDetailCategory.DRIVE_THRU
        );

        // when
        IllegalArgumentException aThrows1 = assertThrows(IllegalArgumentException.class, () -> new Business("", businessCategory));
        IllegalArgumentException aThrows2 = assertThrows(IllegalArgumentException.class, () -> new Business("mom's touch", null));

        // then
        assertEquals(aThrows1.getMessage(), "Business name cannot be empty.");
        assertEquals(aThrows2.getMessage(), "Business category cannot be null.");
    }

    @Test
    @DisplayName("Business - changeBusinessCategory() - changed")
    void should_notSame_when_changeBusinessCategory() {
        // given
        BusinessCategory businessCategory = new BusinessCategory(
                BusinessMajorCategory.RESTAURANT,
                BusinessSubCategory.CASUAL_DINING,
                BusinessDetailCategory.DRIVE_THRU
        );
        Business business = new Business(
                "mom's touch", businessCategory
        );

        // when
        BusinessCategory changedBusinessCategory = new BusinessCategory(
                BusinessMajorCategory.RESTAURANT,
                BusinessSubCategory.CASUAL_DINING,
                BusinessDetailCategory.FAMILY_FRIENDLY
        );
        business.changeBusinessCategory(
                changedBusinessCategory
        );

        // then
        assertNotSame(businessCategory, changedBusinessCategory);
    }


}