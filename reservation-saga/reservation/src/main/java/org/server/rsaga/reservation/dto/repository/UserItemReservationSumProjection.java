package org.server.rsaga.reservation.dto.repository;

import org.server.rsaga.common.domain.ForeignKey;

public record UserItemReservationSumProjection(
        ForeignKey userId,
        ForeignKey reservableItemId,
        Long sumQuantity
){
}