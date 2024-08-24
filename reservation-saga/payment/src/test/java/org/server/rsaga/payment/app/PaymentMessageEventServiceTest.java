package org.server.rsaga.payment.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.common.domain.constant.PaymentType;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.payment.domain.Payment;
import org.server.rsaga.payment.domain.Wallet;
import org.server.rsaga.payment.domain.constant.PaymentStatus;
import org.server.rsaga.payment.infra.repository.PaymentCustomRepository;
import org.server.rsaga.payment.infra.repository.PaymentJpaRepository;
import org.server.rsaga.payment.infra.repository.WalletCustomRepository;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.saga.api.SagaMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentMessageEventService tests")
class PaymentMessageEventServiceTest {
    @Mock
    WalletCustomRepository walletCustomRepository;
    @Mock
    PaymentJpaRepository paymentJpaRepository;
    @Mock
    PaymentCustomRepository paymentCustomRepository;

    @InjectMocks
    PaymentMessageEventService paymentMessageEventService;

    @Test
    @DisplayName("consumePaymentEvent() - PaymentType wallet, sufficient  balance - succeed")
    void should_paymentSucceed_when_paymentTypeWalletAndSufficientBalance() {
        // given
        CreateReservationEvent requestPayload = CreateReservationEventBuilder
                .builder()
                .setPaymentRequest(
                        PaymentType.WALLET.name(),
                        1L,
                        1L,
                        -1000L
                )
                .build();

        Message<String, CreateReservationEvent> requestMessage = SagaMessage.of("key", requestPayload, Message.Status.REQUEST);

        Wallet wallet = new Wallet(new ForeignKey(1L), new Money(2000L));
        when(walletCustomRepository.findByUserIdOrElseThrow(any(ForeignKey.class))).thenReturn(wallet);

        // when
        Message<String, CreateReservationEvent> responseMessage = paymentMessageEventService.consumePaymentEvent(requestMessage);

        // then
        assertNotNull(responseMessage);
        assertEquals(Message.Status.RESPONSE_SUCCESS, responseMessage.status());
        verify(walletCustomRepository, only()).findByUserIdOrElseThrow(any(ForeignKey.class));
        verify(paymentJpaRepository, only()).save(any(Payment.class));
    }

    @Test
    @DisplayName("consumePaymentEvent() - PaymentType wallet, compensating logic - compensating succeed")
    void should_compensatingSucceed_when_paymentTypeWalletAndCompensatingRequest() {
        // given
        CreateReservationEvent compensatingRequestPayload = CreateReservationEventBuilder
                .builder()
                .setPaymentRequest(
                        PaymentType.WALLET.name(),
                        1L,
                        1L,
                        1000L
                )
                .build();
        Message<String, CreateReservationEvent> compensatingRequestMessage = SagaMessage.of("key", compensatingRequestPayload, Message.Status.REQUEST);

        Wallet wallet = new Wallet(new ForeignKey(1L), new Money(500L));
        Payment payment = new Payment(new ForeignKey(1L), new ForeignKey(1L), new Money(1000L), PaymentType.WALLET, PaymentStatus.SUCCESS);

        when(walletCustomRepository.findByUserIdOrElseThrow(any(ForeignKey.class))).thenReturn(wallet);
        when(paymentCustomRepository.findByReservationIdAndUserId(any(ForeignKey.class), any(ForeignKey.class))).thenReturn(payment);

        // when
        Message<String, CreateReservationEvent> responseMessage = paymentMessageEventService.consumePaymentEvent(compensatingRequestMessage);

        // then
        assertNotNull(responseMessage);
        assertEquals(Message.Status.RESPONSE_SUCCESS, responseMessage.status());
        assertEquals(1500L, wallet.getBalance().getAmount());
        assertEquals(PaymentStatus.CANCEL, payment.getPaymentStatus());
        verify(walletCustomRepository, only()).findByUserIdOrElseThrow(any(ForeignKey.class));
        verify(paymentCustomRepository, only()).findByReservationIdAndUserId(any(ForeignKey.class), any(ForeignKey.class));
        verify(paymentJpaRepository, never()).save(any(Payment.class)); // No new payment should be saved in this case
    }

    @Test
    @DisplayName("consumePaymentEvent() - invalid payment type - throw")
    void should_throw_when_invalidPaymentType() {
        // given
        CreateReservationEvent requestPayload = CreateReservationEventBuilder
                .builder()
                .setPaymentRequest("invalid payment type", 1L, 1L, 1000L)
                .build();
        Message<String, CreateReservationEvent> invalidRequestMessage = SagaMessage.of("key", requestPayload, Message.Status.REQUEST);

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> paymentMessageEventService.consumePaymentEvent(invalidRequestMessage));

        // then
        assertEquals("Invalid paymentType.", exception.getMessage());
    }
}