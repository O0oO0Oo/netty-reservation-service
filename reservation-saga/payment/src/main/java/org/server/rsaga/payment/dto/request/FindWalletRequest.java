package org.server.rsaga.payment.dto.request;

import jakarta.validation.constraints.NotNull;

public record FindWalletRequest(
        @NotNull(message = "userId 는 필수 입력 값입니다")
        Long userId
) {
}
