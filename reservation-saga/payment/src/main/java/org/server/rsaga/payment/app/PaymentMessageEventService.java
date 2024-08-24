package org.server.rsaga.payment.app;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.payment.domain.Payment;
import org.server.rsaga.payment.domain.Wallet;
import org.server.rsaga.payment.domain.constant.PaymentStatus;
import org.server.rsaga.common.domain.constant.PaymentType;
import org.server.rsaga.payment.infra.repository.PaymentCustomRepository;
import org.server.rsaga.payment.infra.repository.PaymentJpaRepository;
import org.server.rsaga.payment.infra.repository.WalletCustomRepository;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.saga.api.SagaMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentMessageEventService {
    private final WalletCustomRepository walletCustomRepository;
    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentCustomRepository paymentCustomRepository;

    @Transactional
    public Message<String, CreateReservationEvent> consumePaymentEvent(Message<String, CreateReservationEvent> message) {
        CreateReservationEvent requestPayload = message.payload();
        String paymentType = requestPayload.getPay().getPaymentType();

        if (PaymentType.WALLET.name().equals(paymentType)) {
            return payFromWallet(message);
        }
        // todo 다른 결제 타입 미구현
        else {
            throw new IllegalArgumentException("Invalid paymentType.");
        }
    }

    private Message<String, CreateReservationEvent> payFromWallet(Message<String, CreateReservationEvent> message) {
        CreateReservationEvent requestPayload = message.payload();
        long userId = requestPayload.getPay().getUserId();
        long reservationId = requestPayload.getPay().getReservationId();
        long requestAmount = requestPayload.getPay().getAmount();

        Wallet wallet = walletCustomRepository.findByUserIdOrElseThrow(
                new ForeignKey(userId)
        );

        if (requestAmount < 0) {
            Money price = new Money(-requestAmount);
            wallet.subtractBalance(price);

            Payment payment = new Payment(
                    new ForeignKey(userId),
                    new ForeignKey(reservationId),
                    price,
                    PaymentType.WALLET,
                    PaymentStatus.SUCCESS
            );
            paymentJpaRepository.save(payment);
        }
        else {
            // compensating
            Payment payment = paymentCustomRepository.findByReservationIdAndUserId(
                    new ForeignKey(reservationId), new ForeignKey(userId)
            );
            payment.cancel();

            wallet.addBalance(new Money(requestAmount));
        }

        String key = String.valueOf(userId);
        CreateReservationEvent responsePayload = CreateReservationEventBuilder
                .builder()
                .setPaymentResponse(userId)
                .build();
        return SagaMessage.of(key, responsePayload, message.metadata(), Message.Status.RESPONSE_SUCCESS);
    }
}