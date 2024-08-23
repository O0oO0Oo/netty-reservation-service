package org.server.rsaga.business.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.business.infra.repository.BusinessJpaRepository;
import org.server.rsaga.common.event.BusinessValidationEvent;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessApplicationEventService tests")
class BusinessApplicationEventServiceTest {
    @Mock
    BusinessJpaRepository businessJpaRepository;
    @InjectMocks
    BusinessApplicationEventService businessApplicationEventService;

    @Nested
    @DisplayName("handleBusinessValidationEvent()")
    class HandleBusinessValidationEvent {
        BusinessValidationEvent event = new BusinessValidationEvent(1L);

        @Test
        @DisplayName("exist business - success")
        void should_success_when_existBusiness() {
            // given
            when(businessJpaRepository.existsByIdAndClosedFalse(1L))
                    .thenReturn(true);

            // when
            assertDoesNotThrow(() ->
                    businessApplicationEventService.handleBusinessValidationEvent(event)
            );

            // then
            verify(businessJpaRepository, only().description("The method 'existsByIdAndClosedFalse' should be called only once"))
                    .existsByIdAndClosedFalse(1L);
        }

        @Test
        @DisplayName("non-exist business - throw")
        void should_throw_when_nonExistBusiness() {
            // given
            when(businessJpaRepository.existsByIdAndClosedFalse(1L))
                    .thenReturn(false);

            // when
            CustomException customException = assertThrows(CustomException.class, () ->
                    businessApplicationEventService.handleBusinessValidationEvent(event)
            );

            // then
            verify(businessJpaRepository, only().description("The method 'existsByIdAndClosedFalse' should be called only once"))
                    .existsByIdAndClosedFalse(1L);
            assertEquals(ErrorCode.BUSINESS_NOT_FOUND, customException.getErrorCode(),
                    "The errorCode should be 'BUSINESS_NOT_FOUND'"
            );
        }
    }
}