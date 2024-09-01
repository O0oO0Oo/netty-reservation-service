package org.server.rsaga.reservableitem.infra.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
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

import java.util.Iterator;
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

    // todo : Criteria 로 변경
    @Override
    public List<ReservableItem> findExactMatchReservableItemsWithTimesBatch(Set<ReservableItemSearchDTO> ids) {
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT ri FROM ReservableItem ri " +
                "JOIN FETCH ri.reservableTimes rt WHERE ");

        int index = 0;

        for (ReservableItemSearchDTO ignored : ids) {
            if (index > 0) {
                jpql.append(" OR ");
            }
            jpql.append("(ri.id = :reservableItemId").append(index)
                    .append(" AND ri.businessId = :businessId").append(index)
                    .append(" AND rt.id = :reservableTimeId").append(index).append(")");
            index++;
        }

        Query query = entityManager.createQuery(jpql.toString(), ReservableItem.class);

        // 초기화
        index = 0;

        for (ReservableItemSearchDTO dto : ids) {
            query.setParameter("reservableItemId" + index, dto.reservableItemId());
            query.setParameter("businessId" + index, dto.businessId());
            query.setParameter("reservableTimeId" + index, dto.reservableTimeId());
            index++;
        }

        return query.getResultList();
    }

    @Override
    public List<ReservableItem> findByIdAndReservableTimeWithBatch(Set<ReservableItemQueryDto> ids) {
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT ri FROM ReservableItem ri " +
                "JOIN FETCH ri.reservableTimes rt WHERE ");

        int index = 0;

        for (ReservableItemQueryDto id : ids) {
            if (index > 0) {
                jpql.append(" OR ");
            }
            jpql.append("(ri.id = :reservableItemId").append(index)
                    .append(" AND rt.id = :reservableTimeId").append(index).append(")");
            index++;
        }

        Query query = entityManager.createQuery(jpql.toString(), ReservableItem.class);

        // 초기화
        index = 0;

        for (ReservableItemQueryDto dto : ids) {
            query.setParameter("reservableItemId" + index, dto.reservableItemId());
            query.setParameter("reservableTimeId" + index, dto.reservableTimeId());
            index++;
        }

        return query.getResultList();
    }
}