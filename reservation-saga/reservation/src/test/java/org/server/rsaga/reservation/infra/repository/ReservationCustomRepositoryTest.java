package org.server.rsaga.reservation.infra.repository;

import io.hypersistence.tsid.TSID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.reservation.domain.Reservation;
import org.server.rsaga.reservation.domain.constant.ReservationStatus;
import org.server.rsaga.reservation.dto.repository.UserItemPairDto;
import org.server.rsaga.reservation.dto.repository.UserItemReservationSumProjection;
import org.server.rsaga.reservation.infra.repository.impl.ReservationCustomRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("dev")
@Import(ReservationCustomRepositoryImpl.class)
@DisplayName("ReservationCustomRepository tests")
class ReservationCustomRepositoryTest {
    @Autowired
    private ReservationCustomRepository reservationCustomRepository;
    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Test
    @DisplayName("findSumQuantityByUserIdAndReservableItemIdIn() - return correct sum quantities")
    void should_returnTwoResult_when_findSumQuantityByUserIdAndReservableItemIdIn() {
        // given
        ForeignKey userId1 = new ForeignKey(1L);
        ForeignKey userId2 = new ForeignKey(2L);
        ForeignKey itemId1 = new ForeignKey(1L);
        ForeignKey itemId2 = new ForeignKey(2L);

        Reservation reservation1 = new Reservation(
                TSID.fast().toLong(), 1L, userId1.getId(), itemId1.getId(), 1L, 1L);
        reservation1.updateStatus(ReservationStatus.RESERVED);

        Reservation reservation2 = new Reservation(
                TSID.fast().toLong(), 1L, userId1.getId(), itemId1.getId(), 1L, 3L);
        reservation2.updateStatus(ReservationStatus.COMPLETED);

        Reservation reservation3 = new Reservation(
                TSID.fast().toLong(), 1L, userId2.getId(), itemId2.getId(), 1L, 2L);
        reservation3.updateStatus(ReservationStatus.RESERVED);

        Reservation reservation4 = new Reservation(
                TSID.fast().toLong(), 1L, userId2.getId(), itemId2.getId(), 1L, 2L);
        reservation3.updateStatus(ReservationStatus.FAILED);

        reservationJpaRepository.saveAll(Arrays.asList(reservation1, reservation2, reservation3, reservation4));

        // when
        Set<UserItemPairDto> userItemPairDtos = Set.of(
                new UserItemPairDto(userId1, itemId1),
                new UserItemPairDto(userId2, itemId2)
        );

        List<UserItemReservationSumProjection> result = reservationCustomRepository.findSumQuantityByUserIdAndReservableItemIdIn(userItemPairDtos);

        // then
        assertThat(result).hasSize(2);

        // 결과값이 하나씩 있어야함.
        UserItemReservationSumProjection sumDto1 = result.stream()
                .filter(dto -> dto.userId().equals(userId1) && dto.reservableItemId().equals(itemId1))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected result for userId1 and itemId1 not found."));

        UserItemReservationSumProjection sumDto2 = result.stream()
                .filter(dto -> dto.userId().equals(userId2) && dto.reservableItemId().equals(itemId2))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected result for userId2 and itemId2 not found."));

        // quantity 검사
        assertThat(sumDto1.sumQuantity()).isEqualTo(4L); // user 1, item 1 = 1 + 3, 4개 예약
        assertThat(sumDto2.sumQuantity()).isEqualTo(2L); // user 2, item 2 = 2 , 2개 예약, FAILED 는 처리되지 말아야함
    }
}