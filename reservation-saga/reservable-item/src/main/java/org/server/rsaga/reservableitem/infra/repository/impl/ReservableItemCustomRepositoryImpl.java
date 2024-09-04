package org.server.rsaga.reservableitem.infra.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.reservableitem.domain.ReservableItem;
import org.server.rsaga.reservableitem.dto.repository.ReservableItemQueryDto;
import org.server.rsaga.reservableitem.dto.repository.ReservableItemSearchDTO;
import org.server.rsaga.reservableitem.infra.repository.ReservableItemCustomRepository;
import org.server.rsaga.reservableitem.infra.repository.ReservableItemJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ReservableItemCustomRepositoryImpl implements ReservableItemCustomRepository {
    private final ReservableItemJpaRepository reservableItemJpaRepository;
    private final EntityManager entityManager;

    public ReservableItem findReservableItemByIdAndBusinessIdOrElseThrow(Long reservableItemId, ForeignKey businessId) {
        return reservableItemJpaRepository.findByIdAndBusinessId(
                        reservableItemId,
                        businessId
                )
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
    public ReservableItem findByIdAndBusinessIdAndReservableTimeIdOrElseThrow(Long itemId, ForeignKey businessId, Long timeId) {
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

    @Override
    public List<ReservableItem> findExactMatchReservableItemsWithTimesBatch(Set<ReservableItemSearchDTO> ids) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ReservableItem> query = cb.createQuery(ReservableItem.class);
        Root<ReservableItem> root = query.from(ReservableItem.class);

        root.fetch("reservableTimes", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        for (ReservableItemSearchDTO dto : ids) {
            Predicate idPredicate = cb.equal(root.get("id"), dto.reservableItemId());
            Predicate timePredicate = cb.equal(root.join("reservableTimes").get("id"), dto.reservableTimeId());
            Predicate businessPredicate = cb.equal(root.get("businessId"), dto.businessId());

            Predicate combinedPredicate = cb.and(idPredicate, timePredicate, businessPredicate);
            predicates.add(combinedPredicate);
        }

        query.select(root).distinct(true).where(cb.or(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<ReservableItem> findByIdAndReservableTimeWithBatch(Set<ReservableItemQueryDto> ids) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ReservableItem> query = cb.createQuery(ReservableItem.class);
        Root<ReservableItem> root = query.from(ReservableItem.class);

        root.fetch("reservableTimes", JoinType.INNER);

        ArrayList<Predicate> predicates = new ArrayList<>();
        for (ReservableItemQueryDto dto : ids) {
            Predicate idPredicate = cb.equal(root.get("id"), dto.reservableItemId());
            Predicate timdPredicate = cb.equal(root.get("reservableTimes").get("id"), dto.reservableTimeId());

            Predicate combinePredicate = cb.and(idPredicate, timdPredicate);
            predicates.add(combinePredicate);
        }

        query.select(root).distinct(true).where(cb.or(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }
}