package org.server.rsaga.reservableitem.infra.repository;

import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.reservableitem.domain.ReservableItem;
import org.server.rsaga.reservableitem.dto.repository.ReservableItemQueryDto;
import org.server.rsaga.reservableitem.dto.repository.ReservableItemSearchDTO;

import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public interface ReservableItemCustomRepository {
    ReservableItem findReservableItemByIdAndBusinessIdOrElseThrow(Long reservableItemId, ForeignKey businessId);

    ReservableItem findReservableItemOrElseThrow(Long reservableItemId);

    ReservableItem findByIdAndBusinessIdAndReservableTimeIdOrElseThrow(
            Long itemId,
            ForeignKey businessId,
            Long timeId);

    ReservableItem findByIdAndReservableTimeIdOrElseThrow(Long reservableItemId, Long reservableTimeId);

    List<ReservableItem> findExactMatchReservableItemsWithTimesBatch(Set<ReservableItemSearchDTO> ids);

    List<ReservableItem> findByIdAndReservableTimeWithBatch(Set<ReservableItemQueryDto> ids);
}