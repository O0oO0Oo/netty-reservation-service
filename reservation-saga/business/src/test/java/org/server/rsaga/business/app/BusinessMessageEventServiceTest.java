package org.server.rsaga.business.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.business.VerifyBusinessRequestOuterClass;
import org.server.rsaga.business.VerifyBusinessResponseOuterClass;
import org.server.rsaga.business.domain.Business;
import org.server.rsaga.business.infra.repository.BusinessCustomRepository;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.saga.api.SagaMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessMessageEventService tests")
class BusinessMessageEventServiceTest {
    @Mock
    BusinessCustomRepository businessCustomRepository;
    @InjectMocks
    BusinessMessageEventService businessMessageEventService;


    @Nested
    @DisplayName("consumeVerifyBusinessEvent() tests")
    class ConsumeVerifyBusinessEvent {
        @Mock
        SagaMessage<String, CreateReservationEvent> message;
        @Mock
        CreateReservationEvent event;
        @Mock
        VerifyBusinessRequestOuterClass.VerifyBusinessRequest verifyBusiness;
        @Mock
        Business business;
        Long BUSINESS_ID = 1L;

        @Test
        @DisplayName("exist business - success")
        void should_success_when_existBusiness() {
            // given
            when(message.payload()).thenReturn(event);
            when(event.getVerifyBusiness()).thenReturn(verifyBusiness);
            when(event.getVerifyBusiness().getBusinessId()).thenReturn(BUSINESS_ID);
            when(businessCustomRepository.findByIdOrElseThrow(BUSINESS_ID)).thenReturn(business);
            when(business.getId()).thenReturn(BUSINESS_ID);

            // when
            Message<String, CreateReservationEvent> responseMessage = businessMessageEventService.consumeVerifyBusinessEvent(message);

            // then
            VerifyBusinessResponseOuterClass.VerifyBusinessResponse verifiedBusiness = responseMessage.payload()
                    .getVerifiedBusiness();
            Long key = Long.valueOf(responseMessage.key());
            assertEquals(BUSINESS_ID, key, "The key should be 1L.");
            assertEquals(BUSINESS_ID, verifiedBusiness.getBusinessId(), "The payload value should be 1L.");
            verify(businessCustomRepository, only().description("The findByIdOrElseThrow() method should be executed once"))
                    .findByIdOrElseThrow(BUSINESS_ID);
        }


        @Test
        @DisplayName("non-exist business - throw")
        void should_throw_when_nonExistBusiness() {
            //
            when(message.payload()).thenReturn(event);
            when(event.getVerifyBusiness()).thenReturn(verifyBusiness);
            when(event.getVerifyBusiness().getBusinessId()).thenReturn(BUSINESS_ID);
            when(businessCustomRepository.findByIdOrElseThrow(BUSINESS_ID)).thenThrow(new CustomException(ErrorCode.BUSINESS_NOT_FOUND));

            // when
            CustomException customException = assertThrows(CustomException.class, () -> businessMessageEventService.consumeVerifyBusinessEvent(message));

            // then
            assertEquals(ErrorCode.BUSINESS_NOT_FOUND, customException.getErrorCode(),
                    "The error code should be 'BUSINESS_NOT_FOUND'."
                    );
            verify(business, never().description("The getId() method should never be executed.")).getId();
            verify(businessCustomRepository, only().description("The findByIdOrElseThrow() method should be executed once"))
                    .findByIdOrElseThrow(BUSINESS_ID);
        }
    }
}