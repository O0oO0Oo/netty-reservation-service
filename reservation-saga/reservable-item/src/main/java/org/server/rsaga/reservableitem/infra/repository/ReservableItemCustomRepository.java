package org.server.rsaga.reservableitem.infra.repository;

import org.server.rsaga.reservableitem.domain.ReservableItem;

public interface ReservableItemCustomRepository {
    ReservableItem findReservableItemByIdAndBusinessIdOrElseThrow(Long reservableItemId, Long businessId);

    ReservableItem findReservableItemOrElseThrow(Long reservableItemId);

    ReservableItem findByIdAndBusinessIdAndReservableTimeIdOrElseThrow(
            Long itemId,
            Long businessId,
            Long timeId);

    ReservableItem findByIdAndReservableTimeIdOrElseThrow(Long reservableItemId, Long reservableTimeId);
}