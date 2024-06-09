package com.server.reservation.reservationrecord.entity;

import com.server.reservation.reservableitem.entity.ReservableItem;
import com.server.reservation.reservableitem.repository.ReservableItemRepository;
import com.server.reservation.reservationrecord.repository.ReservationRecordRepository;
import com.server.reservation.business.entity.BusinessType;
import com.server.reservation.business.entity.Business;
import com.server.reservation.business.repository.BusinessRepository;
import com.server.reservation.user.entity.User;
import com.server.reservation.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class ReservationRecordTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ReservableItemRepository reservableItemRepository;
    @Autowired
    private ReservationRecordRepository reservationRecordRepository;

    User user;
    Business business;
    ReservableItem reservableItem;
    long initialItemQuantity = 100;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
                .name("test")
                .balance(1000L)
                .build();
        userRepository.save(user);

        business = Business.builder()
                .businessType(BusinessType.ACCOMMODATION)
                .name("hotel test")
                .build();
        businessRepository.save(business);

        reservableItem = ReservableItem.builder()
                .business(business)
                .maxQuantityPerUser(3L)
                .price(1000L)
                .quantity(initialItemQuantity)
                .name("test Item")
                .build();
        reservableItemRepository.save(reservableItem);
    }

    @Test
    @DisplayName("save - Reservation Record")
    public void givenEntity_whenSave_thenSaveEqualRecord(){
        // Given
        long buyQuantity = 5;
        ReservationRecord record = ReservationRecord.builder()
                .reservationRecordStatus(ReservationRecordStatus.RESERVED)
                .quantity(buyQuantity)
                .user(user)
                .business(business)
                .reservableItem(reservableItem)
                .build();

        reservableItem.setQuantity(reservableItem.getQuantity() - buyQuantity);

        // When
        ReservationRecord save = reservationRecordRepository.save(record);

        // Then
        Assertions.assertEquals(save.getId(), record.getId());
        Assertions.assertEquals(reservableItem.getQuantity(), initialItemQuantity - buyQuantity);
    }
}