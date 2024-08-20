package org.server.rsaga.reservableitem.dto.request;

import jakarta.validation.constraints.NotNull;

public record DeleteReservableItemRequest(
        @NotNull(message = "회사 id 는 필수 입니다.")
        Long businessId
) {
}
