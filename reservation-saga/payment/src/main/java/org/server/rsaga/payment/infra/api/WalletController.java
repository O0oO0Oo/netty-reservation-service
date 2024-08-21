package org.server.rsaga.payment.infra.api;

import io.netty.handler.codec.http.FullHttpResponse;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.dto.FullHttpResponseBuilder;
import org.server.rsaga.payment.app.WalletApiService;
import org.server.rsaga.payment.dto.request.FindWalletRequest;
import org.server.rsaga.payment.dto.request.ModifyWalletRequest;
import org.springframework.web.bind.annotation.*;

@RestController("/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletApiService walletApiService;

    @GetMapping("/{wallet_id}")
    public FullHttpResponse findWallet(@PathVariable("wallet_id") Long walletId, @RequestBody FindWalletRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        walletApiService.findWallet(walletId, request)
                )
                .build();
    }

    @PutMapping("/{wallet_id}/deposit")
    public FullHttpResponse deposit(@PathVariable("wallet_id") Long walletId, @RequestBody ModifyWalletRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        walletApiService.deposit(walletId, request)
                )
                .build();
    }

    @PutMapping("/{wallet_id}/withdraw")
    public FullHttpResponse withdraw(@PathVariable("wallet_id") Long walletId, @RequestBody ModifyWalletRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        walletApiService.withdraw(walletId, request)
                )
                .build();
    }
}