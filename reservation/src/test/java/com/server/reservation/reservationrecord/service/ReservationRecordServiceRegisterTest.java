package com.server.reservation.reservationrecord.service;

import com.server.reservation.business.dto.RegisterBusinessRequest;
import com.server.reservation.business.entity.Business;
import com.server.reservation.business.entity.BusinessType;
import com.server.reservation.business.service.BusinessService;
import com.server.reservation.common.exception.CustomException;
import com.server.reservation.common.exception.ErrorCode;
import com.server.reservation.reservableitem.dto.RegisterReservableItemRequest;
import com.server.reservation.reservableitem.entity.ReservableItem;
import com.server.reservation.reservableitem.repository.ReservableItemRepository;
import com.server.reservation.reservableitem.service.ReservableItemService;
import com.server.reservation.reservationrecord.dto.RegisterReservationRecordRequest;
import com.server.reservation.reservationrecord.entity.ReservationRecord;
import com.server.reservation.user.dto.RegisterUserRequest;
import com.server.reservation.user.entity.User;
import com.server.reservation.user.service.UserService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.plaf.synth.SynthButtonUI;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationRecordServiceRegisterTest {
    @Autowired
    UserService userService;
    @Autowired
    ReservationRecordService reservationRecordService;
    @Autowired
    ReservableItemService reservableItemService;
    @Autowired
    ReservableItemRepository reservableItemRepository;
    @Autowired
    BusinessService businessService;

    User user;
    Business business;
    ReservableItem datePassedItem;
    ReservableItem notAvailableItem;
    ReservableItem normalItem;
    ReservableItem quantityItem;
    ReservableItem expensiveItem;

    @BeforeEach
    void beforeEach() {
        RegisterUserRequest registerUserRequest = new RegisterUserRequest("test");
        user = userService.registerUser(
                registerUserRequest
        );
        user.setBalance(1000L);

        RegisterBusinessRequest registerBusinessRequest = new RegisterBusinessRequest("test-business", BusinessType.STORE);
        business = businessService.registerBusiness(
                registerBusinessRequest
        );

        datePassedItem = ReservableItem.builder()
                .reservableTime(new Date(System.currentTimeMillis() - 1000000000L))
                .isAvailable(true)
                .price(100L)
                .quantity(2L)
                .maxQuantityPerUser(1L)
                .name("test-item")
                .business(business)
                .build();
        reservableItemRepository.save(datePassedItem);

        notAvailableItem = ReservableItem.builder()
                .reservableTime(new Date(System.currentTimeMillis() + 1000000000L))
                .isAvailable(false)
                .price(100L)
                .quantity(2L)
                .maxQuantityPerUser(1L)
                .name("test-item")
                .business(business)
                .build();
        reservableItemRepository.save(notAvailableItem);

        RegisterReservableItemRequest normalRequest = new RegisterReservableItemRequest(
                "test-item", 10L, 3L, new Date(System.currentTimeMillis() + 1000000000L), 10L
        );
        normalItem = reservableItemService.registerBusinessReservableItem(
                business.getId(), normalRequest
        );

        RegisterReservableItemRequest quantityRequest = new RegisterReservableItemRequest(
                "test-item", 10L, 100L, new Date(System.currentTimeMillis() + 1000000000L), 10L
        );
        quantityItem = reservableItemService.registerBusinessReservableItem(
                business.getId(), quantityRequest
        );


        RegisterReservableItemRequest expensiveNormalRequest = new RegisterReservableItemRequest(
                "test-item", 2L, 1L, new Date(System.currentTimeMillis() + 1000000000L), 100000L
        );
        expensiveItem = reservableItemService.registerBusinessReservableItem(
                business.getId(), expensiveNormalRequest
        );
    }

    @Test
    @DisplayName("register - fail - 종료된 상품")
    void givenRequest_whenRegisterNotAvailableItem_thenFailed(){
        // Given
        RegisterReservationRecordRequest registerReservationRecordRequest = new RegisterReservationRecordRequest(
                business.getId(),
                notAvailableItem.getId(),
                1L
        );

        // When, Then
        CustomException exception = assertThrows(
                CustomException.class, () -> reservationRecordService.registerReservationRecord(
                        user.getId(), registerReservationRecordRequest
                )
        );
        assertEquals(ErrorCode.RESERVATION_ITEM_IS_NOT_AVAILABLE.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("register - fail - 날짜 지난 상품")
    void givenRequest_whenRegisterDatePassedItem_thenFailed(){
        // Given
        RegisterReservationRecordRequest registerReservationRecordRequest = new RegisterReservationRecordRequest(
                business.getId(),
                datePassedItem.getId(),
                1L
        );

        // When. Then
        CustomException exception = assertThrows(
                CustomException.class, () -> reservationRecordService.registerReservationRecord(
                        user.getId(), registerReservationRecordRequest
                )
        );
        assertEquals(ErrorCode.RESERVATION_DATE_PASSED.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("register - fail - 인당 구매제한 수량 초과 - 인당 3개 구매 가능 2개 구매 2번")
    void givenRequest_whenRegisterTwiceExceedMaxPurchaseQuantity_thenFailed(){
        // Given
        RegisterReservationRecordRequest registerReservationRecordRequest = new RegisterReservationRecordRequest(
                business.getId(),
                normalItem.getId(),
                2L
        );

        // When. Then
        assertDoesNotThrow(
                () -> reservationRecordService.registerReservationRecord(
                        user.getId(), registerReservationRecordRequest
                )
        );
        CustomException exception = assertThrows(
                CustomException.class, () -> reservationRecordService.registerReservationRecord(
                        user.getId(), registerReservationRecordRequest
                )
        );
        assertEquals(ErrorCode.MAX_QUANTITY_EXCEEDED.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("register - fail - 인당 구매제한 수량 초과 - 인당 3개 구매 가능 한번에 4개 구매")
    void givenRequest_whenRegisterOnceExceedMaxPurchaseQuantity_thenFailed(){
        // Given
        RegisterReservationRecordRequest registerReservationRecordRequest = new RegisterReservationRecordRequest(
                business.getId(),
                normalItem.getId(),
                4L
        );

        // When. Then
        CustomException exception = assertThrows(
                CustomException.class, () -> reservationRecordService.registerReservationRecord(
                        user.getId(), registerReservationRecordRequest
                )
        );
        assertEquals(ErrorCode.MAX_QUANTITY_EXCEEDED.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("register - fail - 구매 수량이 남은 수량 초과")
    void givenRequest_whenRegisterExceedRemainingItemQuantity_thenFailed(){
        // Given
        RegisterReservationRecordRequest registerReservationRecordRequest = new RegisterReservationRecordRequest(
                business.getId(),
                quantityItem.getId(),
                11L // 10 개 남음
        );

        // When. Then
        assertThrows(
                ConstraintViolationException.class, () -> reservationRecordService.registerReservationRecord(
                        user.getId(), registerReservationRecordRequest
                )
        );
    }

    @Test
    @DisplayName("register - fail - 잔고 부족")
    void givenRequest_whenRegisterExceedRemainingUserBalance_thenFailed(){
        // Given
        RegisterReservationRecordRequest registerReservationRecordRequest = new RegisterReservationRecordRequest(
                business.getId(),
                expensiveItem.getId(),
                1L
        );

        // When. Then
        assertThrows(
                ConstraintViolationException.class, () -> reservationRecordService.registerReservationRecord(
                        user.getId(), registerReservationRecordRequest
                )
        );
    }

    @Test
    @DisplayName("register - succeed - 정상 요청, 유저 잔액 감소, 아이템 재고 감소, 예약 레코드 생성")
    void givenRequest_whenRegisterValidRequest_thenSucceed(){
        // Given
        long curBalance = user.getBalance();
        long curQuantity = normalItem.getQuantity();
        RegisterReservationRecordRequest registerReservationRecordRequest = new RegisterReservationRecordRequest(
                business.getId(),
                normalItem.getId(),
                1L
        );

        // When.
        ReservationRecord save = reservationRecordService.registerReservationRecord(user.getId(), registerReservationRecordRequest);

        // Then
        assertAll(
                "정상 요청",
                () -> assertEquals(curBalance - normalItem.getPrice(), user.getBalance()),
                () -> assertEquals(curQuantity - 1, normalItem.getQuantity()),
                () -> assertEquals(1, save.getQuantity()),
                () -> assertEquals(user.getId(), save.getUser().getId()),
                () -> assertEquals(normalItem.getId(), save.getReservableItem().getId())
        );
    }
}