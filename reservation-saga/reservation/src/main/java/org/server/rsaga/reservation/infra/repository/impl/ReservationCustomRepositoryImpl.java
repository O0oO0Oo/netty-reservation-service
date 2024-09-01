package org.server.rsaga.reservation.infra.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.reservation.domain.Reservation;
import org.server.rsaga.reservation.dto.repository.UserItemPairDto;
import org.server.rsaga.reservation.dto.repository.UserItemReservationSumProjection;
import org.server.rsaga.reservation.infra.repository.ReservationCustomRepository;
import org.server.rsaga.reservation.infra.repository.ReservationJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ReservationCustomRepositoryImpl implements ReservationCustomRepository {
    private final ReservationJpaRepository reservationJpaRepository;
    private final EntityManager entityManager;

    @Override
    public Reservation findReservationByIdOrElseThrow(long reservationId) {
        return reservationJpaRepository.findById(reservationId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND)
                );
    }

    @Override
    public Reservation findReservationByIdAndUserIdOrElseThrow(long reservationId, ForeignKey userId) {
        return reservationJpaRepository.findByIdAndUserId(reservationId, userId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND)
                );
    }

    // todo : 추후 QueryDsl 로 변경
    @Override
    public List<UserItemReservationSumProjection> findSumQuantityByUserIdAndReservableItemIdIn(Set<UserItemPairDto> userItemPairs) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserItemReservationSumProjection> query = cb.createQuery(UserItemReservationSumProjection.class);
        Root<Reservation> root = query.from(Reservation.class);

        // 상태 조건문
        Predicate statusPredicate = root.get("reservationStatus").in(
                org.server.rsaga.reservation.domain.constant.ReservationStatus.RESERVED,
                org.server.rsaga.reservation.domain.constant.ReservationStatus.COMPLETED,
                org.server.rsaga.reservation.domain.constant.ReservationStatus.PENDING
        );

        // user id, reservable item id 매칭 조건문
        List<Predicate> predicates = new ArrayList<>();
        for (UserItemPairDto pair : userItemPairs) {
            Predicate userIdPredicate = cb.equal(root.get("userId"), pair.userId());
            Predicate itemIdPredicate = cb.equal(root.get("reservableItemId"), pair.reservableItemId());
            predicates.add(cb.and(userIdPredicate, itemIdPredicate));
        }

        query.select(cb.construct(UserItemReservationSumProjection.class,
                        root.get("userId"),
                        root.get("reservableItemId"),
                        cb.sum(root.get("quantity"))))
                .where(cb.and(statusPredicate), cb.or(predicates.toArray(new Predicate[0])))
                .groupBy(root.get("userId"), root.get("reservableItemId"));

        List<UserItemReservationSumProjection> results = entityManager.createQuery(query).getResultList();

        // 없는 결과라면 null 이 아닌, userId, reservationId, 0 으로 반환한다.
        List<UserItemReservationSumProjection> finalResults = new ArrayList<>(results);
        for (UserItemPairDto pair : userItemPairs) {
            boolean exists = results.stream().anyMatch(r -> r.userId().equals(pair.userId()) && r.reservableItemId().equals(pair.reservableItemId()));
            if (!exists) {
                // 0 개가 예약되었다는 뜻으로 null 이 아닌 0 개로 만들어서 반환
                finalResults.add(new UserItemReservationSumProjection(pair.userId(), pair.reservableItemId(), 0L));
            }
        }

        return finalResults;
    }
}
