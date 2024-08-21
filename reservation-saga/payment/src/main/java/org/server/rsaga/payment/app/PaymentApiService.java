package org.server.rsaga.payment.app;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.payment.dto.request.FindPaymentRequest;
import org.server.rsaga.payment.dto.response.PaymentDetailsResponse;
import org.server.rsaga.payment.infra.repository.PaymentJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentApiService {
    private final PaymentJpaRepository paymentJpaRepository;

    public List<PaymentDetailsResponse> findPaymentList(FindPaymentRequest request) {
        return PaymentDetailsResponse.of(paymentJpaRepository.findByUserId(
                new ForeignKey(request.userId()))
        );
    }
}