package org.server.rsaga.payment.dto.request;

import jakarta.validation.constraints.NotNull;

public record ModifyWalletRequest(
        @NotNull(message = "userId 는 필수 입력 값 입니다")
        Long userId,
        @NotNull(message = "amount 는 필수 입력 값 입니다.")
        Long amount
) {
}
