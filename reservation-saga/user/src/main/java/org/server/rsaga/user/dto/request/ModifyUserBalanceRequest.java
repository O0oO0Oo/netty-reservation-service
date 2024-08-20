package org.server.rsaga.user.dto.request;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record ModifyUserBalanceRequest(
        @NotNull(message = "금액은 null 이면 안됩니다.")
        @Range(min = 0, message = "금액은 0원 이상이어야 합니다.")
        Long money
) {
}
