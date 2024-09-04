package org.server.rsaga.payment.app;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.common.domain.constant.PaymentType;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.payment.domain.Payment;
import org.server.rsaga.payment.domain.Wallet;
import org.server.rsaga.payment.domain.constant.PaymentStatus;
import org.server.rsaga.payment.infra.repository.PaymentCustomRepository;
import org.server.rsaga.payment.infra.repository.PaymentJpaRepository;
import org.server.rsaga.payment.infra.repository.WalletCustomRepository;
import org.server.rsaga.payment.infra.repository.WalletJpaRepository;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.saga.api.SagaMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMessageEventService {
    private final WalletCustomRepository walletCustomRepository;
    private final WalletJpaRepository walletJpaRepository;
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

    /**
     * {@link org.server.rsaga.payment.PaymentRequestOuterClass.PaymentRequest} 처리 메서드 
     */
    @Transactional
    public List<Message<String, CreateReservationEvent>> consumeBulkPaymentEvent(List<Message<String, CreateReservationEvent>> messages) {
        List<Message<String, CreateReservationEvent>> responses = new ArrayList<>(messages.size());

        // 타입별로 모아서 처리해야 한다.
        Map<PaymentType, List<Message<String, CreateReservationEvent>>> paymentTypeMap = classifyMessagesByPaymentType(messages, responses);

        // 타입별로 리스트에서 하나씩 뽑아서 처리한다.
        responses.addAll(payFromWalletBulk(paymentTypeMap.get(PaymentType.WALLET)));

        return responses;
    }

    /**
     * {@link PaymentType} 에 따른 분류
     */
    private Map<PaymentType, List<Message<String, CreateReservationEvent>>> classifyMessagesByPaymentType(
            List<Message<String, CreateReservationEvent>> messages,
            List<Message<String, CreateReservationEvent>> responses
    ) {
        Map<PaymentType, List<Message<String, CreateReservationEvent>>> messagesByPaymentTypeMap = new EnumMap<>(PaymentType.class);

        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            String stringPaymentType = requestPayload.getPay().getPaymentType();

            try {
                PaymentType paymentType = PaymentType.valueOf(stringPaymentType);

                // 각 처리 메시지 들어감, 하나씩 뽑아서 처리하기에 LinkedList 사용
                messagesByPaymentTypeMap.putIfAbsent(paymentType, new LinkedList<>());
                messagesByPaymentTypeMap.get(paymentType).add(message);
            } catch (IllegalArgumentException e) {
                // 지원하지 않는 타입이라면 실패 응답
                responses.add(SagaMessage.createFailureResponse(message, ErrorCode.PAYMENT_TYPE_NOT_FOUND));
            }
        }
        return messagesByPaymentTypeMap;
    }

    /**
     * {@link PaymentType} WALLET 에 대한 처리
     */
    private List<Message<String, CreateReservationEvent>> payFromWalletBulk(List<Message<String, CreateReservationEvent>> messages) {
        if(messages == null || messages.isEmpty()) return Collections.emptyList();

        // 조회에 필요한 id 추출
        Set<Long> userIds = extractUserIdIdsFromMessages(messages);
        Set<Long> reservationIds = extractReservationIdIdsFromMessages(messages);

        // bulk 조회
        Map<Long, Wallet> walletMapByUserId = findWalletExistingUserIds(userIds);
        Map<Long, Payment> paymentMapByReservationId = findPaymentExistingReservationIds(reservationIds);

        return generateResponseMessages(messages, walletMapByUserId, paymentMapByReservationId);
    }

    /**
     * 메시지에서 user id 추출 
     */
    private Set<Long> extractUserIdIdsFromMessages(List<Message<String, CreateReservationEvent>> messages) {
        Set<Long> userIds = new HashSet<>();
        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            long userId = requestPayload.getPay().getUserId();
            userIds.add(userId);
        }
        return userIds;
    }

    /**
     * 메시지에서 reservation id 추출
     */
    private Set<Long> extractReservationIdIdsFromMessages(List<Message<String, CreateReservationEvent>> messages) {
        Set<Long> reservationIds = new HashSet<>();
        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            long reservationId = requestPayload.getPay().getReservationId();
            reservationIds.add(reservationId);
        }
        return reservationIds;
    }

    private Map<Long, Wallet> findWalletExistingUserIds(Set<Long> userIds) {
        return walletJpaRepository.findAllByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(Wallet::getUserId, wallet -> wallet));
    }

    private Map<Long, Payment> findPaymentExistingReservationIds(Set<Long> reservationIds) {
        return paymentJpaRepository.findAllByReservationIdIn(reservationIds).stream()
                .collect(Collectors.toMap(Payment::getReservationId, payment -> payment));
    }

    private List<Message<String, CreateReservationEvent>> generateResponseMessages(
            List<Message<String, CreateReservationEvent>> messages,
            Map<Long, Wallet> walletMapByUserId,
            Map<Long, Payment> paymentMapByReservationId
            ){
        List<Message<String, CreateReservationEvent>> responses = new ArrayList<>();

        for (Message<String, CreateReservationEvent> message : messages) {
            responses.add(processMessage(message, walletMapByUserId, paymentMapByReservationId));
        }

        // 저장
        saveData(walletMapByUserId, paymentMapByReservationId);

        return responses;
    }

    private Message<String, CreateReservationEvent> processMessage(
            Message<String, CreateReservationEvent> message,
            Map<Long, Wallet> walletMapByUserId,
            Map<Long, Payment> paymentMapByReservationId) {
        CreateReservationEvent requestPayload = message.payload();
        long userId = requestPayload.getPay().getUserId();
        long requestAmount = requestPayload.getPay().getAmount();

        if (!walletMapByUserId.containsKey(userId)) {
            return SagaMessage.createFailureResponse(message, ErrorCode.WALLET_NOT_FOUND);
        }

        Wallet wallet = walletMapByUserId.get(userId);

        return (requestAmount < 0) ?
                handleSubtractBalanceMessage(message, wallet, paymentMapByReservationId) :
                handleAddBalanceMessage(message, wallet, paymentMapByReservationId);
    }

    private void saveData(Map<Long, Wallet> walletMapByUserId, Map<Long, Payment> paymentMapByReservationId) {
        walletJpaRepository.saveAll(walletMapByUserId.values());
        paymentCustomRepository.batchSave(paymentMapByReservationId.values());
    }

    /**
     * 결제 트랜잭션 메시지 처리
     */
    private Message<String, CreateReservationEvent> handleSubtractBalanceMessage(
            Message<String, CreateReservationEvent> message,
            Wallet wallet,
            Map<Long, Payment> paymentMapByReservationId
    ) {
        CreateReservationEvent requestPayload = message.payload();
        long userId = requestPayload.getPay().getUserId();
        long reservationId = requestPayload.getPay().getReservationId();
        long requestAmount = requestPayload.getPay().getAmount();

        try {
            Money price = new Money(-requestAmount);
            wallet.subtractBalance(price);

            // 결제 생성
            Payment payment = new Payment(
                    new ForeignKey(userId),
                    new ForeignKey(reservationId),
                    price,
                    PaymentType.WALLET,
                    PaymentStatus.SUCCESS
            );
            paymentMapByReservationId.put(reservationId, payment);

            // 성공 메시지
            return createSuccessResponse(message, userId);
        } catch (CustomException e) {
            // 잔고 부족, 실패 메시지
            return SagaMessage.createFailureResponse(message, e.getErrorCode());
        } catch (Exception e) {
            return SagaMessage.createFailureResponse(message, e.getMessage());
        }
    }

    /**
     * 결제 보상 트랜잭션 메시지 처리
     */
    private Message<String, CreateReservationEvent> handleAddBalanceMessage(
            Message<String, CreateReservationEvent> message,
            Wallet wallet,
            Map<Long, Payment> paymentMapByReservationId
    ) {
        CreateReservationEvent requestPayload = message.payload();
        long userId = requestPayload.getPay().getUserId();
        long reservationId = requestPayload.getPay().getReservationId();
        long requestAmount = requestPayload.getPay().getAmount();

        Payment payment = paymentMapByReservationId.get(reservationId);

        // 결제기록 찾을 수 없다.
        if (payment == null) {
            return SagaMessage.createFailureResponse(message, ErrorCode.PAYMENT_NOT_FOUND);
        }

        // 결제 취소
        payment.cancel();
        wallet.addBalance(new Money(requestAmount));

        // 성공 메시지
        return createSuccessResponse(message, userId);
    }

    private Message<String, CreateReservationEvent> createSuccessResponse(Message<String, CreateReservationEvent> message, Long userId) {
        String key = String.valueOf(userId);
        CreateReservationEvent responsePayload = CreateReservationEventBuilder
                .builder()
                .setPaymentResponse(userId)
                .build();
        return SagaMessage.of(key, responsePayload, message.metadata(), Message.Status.RESPONSE_SUCCESS);
    }
}