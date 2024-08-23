package org.server.rsaga.business.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.server.rsaga.business.domain.constant.BusinessDetailCategory;
import org.server.rsaga.business.domain.constant.BusinessSubCategory;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BusinessCategory tests")
class BusinessCategoryTest {

    @Test
    @DisplayName("create BusinessCategory - null MajorCategory - fail")
    void should_fail_when_createInvalidParameter() {
        // given

        // when
        IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> new BusinessCategory(
                null,
                BusinessSubCategory.CASUAL_DINING,
                BusinessDetailCategory.DRIVE_THRU
        ));

        // then
        assertEquals("Major category cannot be empty.", aThrows.getMessage());
    }
}