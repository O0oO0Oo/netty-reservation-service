package org.server.rsaga.reservableitem.infra.repository.impl;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.reservableitem.domain.ReservableItem;
import org.server.rsaga.reservableitem.infra.repository.ReservableItemCustomRepository;
import org.server.rsaga.reservableitem.infra.repository.ReservableItemJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReservableItemCustomRepositoryImpl implements ReservableItemCustomRepository {
    private final ReservableItemJpaRepository reservableItemJpaRepository;

    public ReservableItem findReservableItemByIdAndBusinessIdOrElseThrow(Long reservableItemId, Long businessId) {
        return reservableItemJpaRepository.findByIdAndBusinessId(reservableItemId, businessId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.RESERVABLE_ITEM_NOT_FOUND)
                );
    }

    @Override
    public ReservableItem findReservableItemOrElseThrow(Long reservableItemId) {
        return reservableItemJpaRepository.findById(reservableItemId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.RESERVABLE_ITEM_NOT_FOUND)
                );
    }

    @Override
    public ReservableItem findByIdAndBusinessIdAndReservableTimeIdOrElseThrow(Long itemId, Long businessId, Long timeId) {
        return reservableItemJpaRepository.findReservableItemsWithTimes(
                itemId, businessId, timeId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.RESERVABLE_ITEM_NOT_FOUND)
                );
    }

    @Override
    public ReservableItem findByIdAndReservableTimeIdOrElseThrow(Long reservableItemId, Long reservableTimeId) {
        return reservableItemJpaRepository.findByIdAndReservableTimeId(
                reservableItemId, reservableTimeId
        ).orElseThrow(
                () -> new CustomException(ErrorCode.RESERVABLE_ITEM_NOT_FOUND)
        );
    }
}
