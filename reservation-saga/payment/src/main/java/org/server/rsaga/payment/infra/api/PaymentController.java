package org.server.rsaga.payment.infra.api;

import io.netty.handler.codec.http.FullHttpResponse;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.dto.FullHttpResponseBuilder;
import org.server.rsaga.payment.app.PaymentApiService;
import org.server.rsaga.payment.dto.request.FindPaymentRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentApiService paymentApiService;

    @GetMapping
    public FullHttpResponse findPaymentList(@RequestBody FindPaymentRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        paymentApiService.findPaymentList(request)
                )
                .build();
    }
}