package org.server.rsaga.business.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.server.rsaga.business.domain.constant.BusinessDetailCategory;
import org.server.rsaga.business.domain.constant.BusinessMajorCategory;
import org.server.rsaga.business.domain.constant.BusinessSubCategory;
import org.server.rsaga.common.event.BusinessClosedEvent;

import java.util.List;

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

    @Nested
    @DisplayName("Business domain event tests")
    class BusinessDomainEvent {
        private Business business;

        @BeforeEach
        void setUp() {
            BusinessCategory businessCategory = new BusinessCategory(
                    BusinessMajorCategory.STORE,
                    BusinessSubCategory.GROCERY,
                    BusinessDetailCategory.ORGANIC
            );
            business = new Business("Test Business", businessCategory);
        }

        @Test
        @DisplayName("closeBusiness() - Domain event published")
        void should_closedBusinessAndEventPublished_when_closeBusiness() {
            // given

            // when
            business.closeBusiness();

            // then
            List<Object> events = business.getDomainEvents();
            assertEquals(1, events.size(), "Expected exactly one domain event after closing business.");
            assertTrue(events.get(0) instanceof BusinessClosedEvent, "BusinessClosedEvent should have been added");

            BusinessClosedEvent event = (BusinessClosedEvent) events.get(0);
            assertEquals(business.getId(), event.businessId(), "Business ID in the event should match the closed business ID.");
        }

        @Test
        @DisplayName("clearDomainEvents() - Domain events cleared.")
        void clearDomainEvents_ShouldClearEventsAfterPublication() {
            // given

            // when
            business.closeBusiness();
            business.clearDomainEvents();

            // then
            assertTrue(business.getDomainEvents().isEmpty(), "Domain events should be empty after after executing the clearDomainEvents() method.");
        }
    }
}