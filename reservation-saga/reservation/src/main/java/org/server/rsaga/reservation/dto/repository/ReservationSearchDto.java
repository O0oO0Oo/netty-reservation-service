package org.server.rsaga.reservation.dto.repository;

import org.server.rsaga.common.domain.ForeignKey;

public record ReservationSearchDto(
        ForeignKey userId,
        ForeignKey reservableItemId
) {
}
