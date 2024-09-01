package org.server.rsaga.reservableitem.infra.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.common.event.BusinessValidationEvent;
import org.server.rsaga.common.event.EventPublisher;
import org.server.rsaga.reservableitem.domain.ReservableItem;
import org.server.rsaga.reservableitem.domain.ReservableTime;
import org.server.rsaga.reservableitem.domain.Stock;
import org.server.rsaga.reservableitem.domain.constant.Unit;
import org.server.rsaga.reservableitem.dto.repository.ReservableItemQueryDto;
import org.server.rsaga.reservableitem.dto.repository.ReservableItemSearchDTO;
import org.server.rsaga.reservableitem.infra.repository.impl.ReservableItemCustomRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("dev")
@Import(ReservableItemCustomRepositoryImpl.class)
@DisplayName("ReservableItemJpaRepository tests")
class ReservableItemCustomRepositoryTest {
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    ReservableItemCustomRepository reservableItemCustomRepository;

    @Test
    @DisplayName("findExactMatchReservableItemsWithTimesBatch() - succeed")
    public void should_oneSelectQueryAndEagerLoading_when_findExactMatchReservableItemsWithTimesBatch() {
        // Given
        Long businessId1 = 1L;
        ReservableTime time1 = new ReservableTime(
                new Date(System.currentTimeMillis() + 3600 * 1000),
                new Stock(1L, Unit.BOX),
                true
        );
        ReservableTime time2 = new ReservableTime(
                new Date(System.currentTimeMillis() + 3600 * 1000),
                new Stock(1L, Unit.BOX),
                true
        );
        ReservableItem item1;

        Long businessId2 = 2L;
        ReservableTime time3 = new ReservableTime(
                new Date(System.currentTimeMillis() + 3600 * 1000),
                new Stock(1L, Unit.BOX),
                true
        );
        ReservableTime time4 = new ReservableTime(
                new Date(System.currentTimeMillis() + 3600 * 1000),
                new Stock(1L, Unit.BOX),
                true
        );
        ReservableItem item2;

        // 내부에서 스태틱 메서드를 사용함
        try (MockedStatic<EventPublisher> eventPublisherMockedStatic = Mockito.mockStatic(EventPublisher.class)) {
            BusinessValidationEvent businessValidationEvent1 = new BusinessValidationEvent(businessId1);
            eventPublisherMockedStatic
                    .when(
                            () -> EventPublisher.publish(businessValidationEvent1)
                    )
                    .thenAnswer(invocation -> null);

            BusinessValidationEvent businessValidationEvent2 = new BusinessValidationEvent(businessId2);
            eventPublisherMockedStatic
                    .when(
                            () -> EventPublisher.publish(businessValidationEvent2)
                    )
                    .thenAnswer(invocation -> null);

            item1 = new ReservableItem(
                    "B1",
                    1L,
                    List.of(time1, time2)
                    ,
                    new Money(100L),
                    new ForeignKey(businessId1),
                    true
            );
            item2 = new ReservableItem(
                    "B1",
                    1L,
                    List.of(time3, time4)
                    ,
                    new Money(100L),
                    new ForeignKey(businessId2),
                    true
            );
        }

        entityManager.persist(item1);
        entityManager.persist(item2);

        entityManager.flush();
        entityManager.clear();

        Set<ReservableItemSearchDTO> combinations = Set.of(
                new ReservableItemSearchDTO(item1.getId(), new ForeignKey(businessId1), time2.getId()),
                new ReservableItemSearchDTO(item2.getId(), new ForeignKey(businessId2), time3.getId()),
                new ReservableItemSearchDTO(-1L, new ForeignKey(-1L), -1L) // 존재하지 않는 아이템
        );

        // When
        List<ReservableItem> result = reservableItemCustomRepository.findExactMatchReservableItemsWithTimesBatch(combinations);


        // Then
        assertThat(result).hasSize(2); // 총 결과 사이즈
        assertThat(result).extracting("id")
                .containsExactlyInAnyOrder(item1.getId(), item2.getId()); // reservableItem Id 확인
        assertThat(result).extracting("businessId")
                .containsExactlyInAnyOrder(item1.getBusinessId(), item2.getBusinessId()); // Business Id 확인

        // ReservableTimes 확인
        for (ReservableItem item : result) {
            assertThat(item.getReservableTimes()).hasSize(1); // 1개씩 있어야 함.

            if (item.getId().equals(item1.getId())) {
                assertThat(item.getReservableTimes()) // item1 에는 time2 가 있어야 함.
                        .extracting("id").contains(time2.getId());

                Timestamp expectedTime = new Timestamp(time2.getTime().getTime()); // Timestamp 변환
                assertThat(item.getReservableTimes().iterator().next().getTime())
                        .isEqualTo(expectedTime); // time2 와 매칭
            } else {
                assertThat(item.getReservableTimes())
                        .extracting("id").contains(time3.getId()); // item2 에는 time3 가 있어야 함.

                Timestamp expectedTime = new Timestamp(time3.getTime().getTime()); // Timestamp 변환
                assertThat(item.getReservableTimes().iterator().next().getTime())
                        .isEqualTo(expectedTime); // time3 와 매칭
            }
        }

        entityManager.clear(); // 영속성 컨텍스트 초기화
        for (ReservableItem item : result) {
            assertThat(item.getReservableTimes()).isNotEmpty(); // 추가 쿼리 발생하지 않아야 함
        }
    }

    @Test
    @DisplayName("findByIdAndReservableTimeWithBatch() - succeed")
    public void should_returnCorrectReservableItems_when_findByIdAndReservableTimeWithBatch() {
        // Given
        Long businessId1 = 1L;
        ReservableTime time1 = new ReservableTime(
                new Date(System.currentTimeMillis() + 3600 * 1000),
                new Stock(1L, Unit.BOX),
                true
        );
        ReservableTime time2 = new ReservableTime(
                new Date(System.currentTimeMillis() + 7200 * 1000),
                new Stock(1L, Unit.BOX),
                true
        );
        ReservableItem item1;

        Long businessId2 = 2L;
        ReservableTime time3 = new ReservableTime(
                new Date(System.currentTimeMillis() + 3600 * 1000),
                new Stock(1L, Unit.BOX),
                true
        );
        ReservableTime time4 = new ReservableTime(
                new Date(System.currentTimeMillis() + 7200 * 1000),
                new Stock(1L, Unit.BOX),
                true
        );
        ReservableItem item2;

        // Mock static method calls
        try (MockedStatic<EventPublisher> eventPublisherMockedStatic = Mockito.mockStatic(EventPublisher.class)) {
            BusinessValidationEvent businessValidationEvent1 = new BusinessValidationEvent(businessId1);
            eventPublisherMockedStatic
                    .when(
                            () -> EventPublisher.publish(businessValidationEvent1)
                    )
                    .thenAnswer(invocation -> null);

            BusinessValidationEvent businessValidationEvent2 = new BusinessValidationEvent(businessId2);
            eventPublisherMockedStatic
                    .when(
                            () -> EventPublisher.publish(businessValidationEvent2)
                    )
                    .thenAnswer(invocation -> null);

            item1 = new ReservableItem(
                    "B1",
                    1L,
                    List.of(time1, time2),
                    new Money(100L),
                    new ForeignKey(businessId1),
                    true
            );
            item2 = new ReservableItem(
                    "B2",
                    1L,
                    List.of(time3, time4),
                    new Money(200L),
                    new ForeignKey(businessId2),
                    true
            );
        }

        entityManager.persist(item1);
        entityManager.persist(item2);

        entityManager.flush();
        entityManager.clear();

        Set<ReservableItemQueryDto> combinations = Set.of(
                new ReservableItemQueryDto(item1.getId(), time2.getId()),
                new ReservableItemQueryDto(item2.getId(), time3.getId()),
                new ReservableItemQueryDto(-1L, -1L) // 존재하지 않는 아이템
        );

        // When
        List<ReservableItem> result = reservableItemCustomRepository.findByIdAndReservableTimeWithBatch(combinations);

        // Then
        assertThat(result).hasSize(2); // 총 결과 사이즈
        assertThat(result).extracting("id")
                .containsExactlyInAnyOrder(item1.getId(), item2.getId()); // reservableItem Id 확인
        assertThat(result).extracting("businessId")
                .containsExactlyInAnyOrder(item1.getBusinessId(), item2.getBusinessId()); // Business Id 확인

        // ReservableTimes 확인
        for (ReservableItem item : result) {
            assertThat(item.getReservableTimes()).hasSize(1); // 1개씩 있어야 함.

            if (item.getId().equals(item1.getId())) {
                assertThat(item.getReservableTimes()) // item1 에는 time2 가 있어야 함.
                        .extracting("id").contains(time2.getId());

                Timestamp timestamp = new Timestamp(time2.getTime().getTime());
                assertThat(item.getReservableTimes().iterator().next().getTime())
                        .isEqualTo(timestamp); // time2 와 매칭
            } else {
                assertThat(item.getReservableTimes())
                        .extracting("id").contains(time3.getId()); // item2 에는 time3 가 있어야 함.

                Timestamp timestamp = new Timestamp(time3.getTime().getTime());
                assertThat(item.getReservableTimes().iterator().next().getTime())
                        .isEqualTo(timestamp); // time3 와 매칭
            }
        }

        entityManager.clear(); // 영속성 컨텍스트 초기화
        for (ReservableItem item : result) {
            assertThat(item.getReservableTimes()).isNotEmpty(); // 추가 쿼리 발생하지 않아야 함
        }
    }
}