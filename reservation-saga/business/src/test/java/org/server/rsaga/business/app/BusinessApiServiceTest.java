package org.server.rsaga.business.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.business.domain.Business;
import org.server.rsaga.business.domain.BusinessCategory;
import org.server.rsaga.business.domain.constant.BusinessDetailCategory;
import org.server.rsaga.business.domain.constant.BusinessMajorCategory;
import org.server.rsaga.business.domain.constant.BusinessSubCategory;
import org.server.rsaga.business.dto.request.ModifyBusinessRequest;
import org.server.rsaga.business.dto.request.RegisterBusinessRequest;
import org.server.rsaga.business.dto.response.BusinessDetailsResponse;
import org.server.rsaga.business.infra.repository.BusinessCustomRepository;
import org.server.rsaga.business.infra.repository.BusinessJpaRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("BusinessApiService tests")
@ExtendWith(MockitoExtension.class)
class BusinessApiServiceTest {
    @Mock
    BusinessCustomRepository businessCustomRepository;
    @Mock
    BusinessJpaRepository businessJpaRepository;

    @InjectMocks
    private BusinessApiService businessApiService;

    private Business testBusiness;
    private final Long TEST_BUSINESS_ID = 1L;

    @BeforeEach
    void setUp() {
        testBusiness = new Business("Test Business", new BusinessCategory(
                BusinessMajorCategory.RESTAURANT,
                BusinessSubCategory.FAST_FOOD,
                BusinessDetailCategory.DRIVE_THRU
        ));
    }


    @Test
    @DisplayName("registerBusiness() - valid RegisterBusinessRequest - create business")
    void should_createBusiness_when_validRequest() {
        // given
        RegisterBusinessRequest testRequest = new RegisterBusinessRequest(
                "Test Business",
                BusinessMajorCategory.RESTAURANT,
                BusinessSubCategory.FAST_FOOD,
                BusinessDetailCategory.DRIVE_THRU
        );

        when(businessJpaRepository.save(any(Business.class)))
                .thenReturn(testBusiness);

        // when
        BusinessDetailsResponse response = businessApiService.registerBusiness(testRequest);

        // then
        assertEquals(testBusiness.getId(), response.businessId(), "The IDs should match.");
        assertEquals(testBusiness.getName(), response.businessName(), "The business names should match.");
        assertEquals(testBusiness.getMajorCategory(), response.majorCategory(), "The major categories should match.");
        assertEquals(testBusiness.getSubCategory(), response.subCategory(), "The subcategories should match.");
        assertEquals(testBusiness.getDetailCategory(), response.detailCategory(), "The detail categories should match.");
    }

    @Nested
    @DisplayName("modifyBusiness()")
    class ModifyBusiness {

        @Test
        @DisplayName("null name, valid category ModifyBusinessRequest - change category")
        void should_changed_when_nullNameValidCategory() {
            // given
            ModifyBusinessRequest request = new ModifyBusinessRequest(
                    null,
                    BusinessMajorCategory.STORE,
                    BusinessSubCategory.CLOTHING,
                    BusinessDetailCategory.LUXURY
            );

            when(businessCustomRepository.findByIdAndNotClosedOrElseThrow(TEST_BUSINESS_ID))
                    .thenReturn(testBusiness);

            String name = testBusiness.getName();
            BusinessCategory businessCategory = testBusiness.getBusinessCategory();

            // when
            businessApiService.modifyBusiness(TEST_BUSINESS_ID, request);

            // then
            assertEquals(name, testBusiness.getName(), "The business name should match the expected name.");
            assertNotSame(businessCategory, testBusiness.getBusinessCategory(), "The business category should not be same instance.");
            assertNotEquals(businessCategory, testBusiness.getBusinessCategory(), "The business category should not be equal.");
        }

        @Test
        @DisplayName("valid name, null category ModifyBusinessRequest - change name")
        void should_changeName_when_validNameNullCategory() {
            // given
            String changedName = "changedName";
            ModifyBusinessRequest request = new ModifyBusinessRequest(
                    changedName,
                    null, null, null
            );

            when(businessCustomRepository.findByIdAndNotClosedOrElseThrow(TEST_BUSINESS_ID))
                    .thenReturn(testBusiness);

            String name = testBusiness.getName();
            BusinessCategory businessCategory = testBusiness.getBusinessCategory();

            // when
            businessApiService.modifyBusiness(TEST_BUSINESS_ID, request);

            // then
            assertEquals(changedName, testBusiness.getName(), "The business name should match the expected name.");
            assertSame(businessCategory, testBusiness.getBusinessCategory(), "The business category should be same instance.");
            assertEquals(businessCategory, testBusiness.getBusinessCategory(), "The business category should be equal.");
        }

        @Test
        @DisplayName("valid name, invalid category ModifyBusinessRequest - throw")
        void should_throw_when_validNameInvalidCategory() {
            // given
            ModifyBusinessRequest request = new ModifyBusinessRequest(
                    null,
                    BusinessMajorCategory.STORE,
                    null,
                    BusinessDetailCategory.LUXURY
            );

            when(businessCustomRepository.findByIdAndNotClosedOrElseThrow(TEST_BUSINESS_ID))
                    .thenReturn(testBusiness);

            String name = testBusiness.getName();
            BusinessCategory businessCategory = testBusiness.getBusinessCategory();

            // when
            IllegalArgumentException aThrows =
                    assertThrows(IllegalArgumentException.class, () -> businessApiService.modifyBusiness(TEST_BUSINESS_ID, request));

            // then
            assertEquals("In order for DetailCategory to be set, Subcategory should not be null.", aThrows.getMessage());
            assertEquals(name, testBusiness.getName(), "The business name should match the expected name.");
            assertSame(businessCategory, testBusiness.getBusinessCategory(), "The business category should be same instance.");
            assertEquals(businessCategory, testBusiness.getBusinessCategory(), "The business category should be equal.");
        }
    }

    @Test
    @DisplayName("deleteBusiness() - valid request - business closed")
    void should_businessClosed_when_deleteValidBusinessId() {
        // given
        when(businessCustomRepository.findByIdOrElseThrow(TEST_BUSINESS_ID))
                .thenReturn(testBusiness);

        // when
        businessApiService.deleteBusiness(TEST_BUSINESS_ID);

        // then
        assertTrue(testBusiness.isClosed(), "The isClosed() value should be true.");
    }
}