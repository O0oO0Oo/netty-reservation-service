package org.server.rsaga.reservableitem.infra.repository;

import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.reservableitem.domain.ReservableItem;

public interface ReservableItemCustomRepository {
    ReservableItem findReservableItemByIdAndBusinessIdOrElseThrow(Long reservableItemId, ForeignKey businessId);

    ReservableItem findReservableItemOrElseThrow(Long reservableItemId);

    ReservableItem findByIdAndBusinessIdAndReservableTimeIdOrElseThrow(
            Long itemId,
            ForeignKey businessId,
            Long timeId);

    ReservableItem findByIdAndReservableTimeIdOrElseThrow(Long reservableItemId, Long reservableTimeId);
}