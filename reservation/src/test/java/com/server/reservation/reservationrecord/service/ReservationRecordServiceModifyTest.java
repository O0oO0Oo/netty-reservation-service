package com.server.reservation.reservationrecord.service;

import com.server.reservation.business.dto.RegisterBusinessRequest;
import com.server.reservation.business.entity.Business;
import com.server.reservation.business.entity.BusinessType;
import com.server.reservation.business.service.BusinessService;
import com.server.reservation.common.exception.CustomException;
import com.server.reservation.common.exception.ErrorCode;
import com.server.reservation.reservableitem.entity.ReservableItem;
import com.server.reservation.reservableitem.repository.ReservableItemRepository;
import com.server.reservation.reservableitem.service.ReservableItemService;
import com.server.reservation.reservationrecord.dto.ModifyReservationRecordRequest;
import com.server.reservation.reservationrecord.dto.RegisterReservationRecordRequest;
import com.server.reservation.reservationrecord.entity.ReservationRecord;
import com.server.reservation.reservationrecord.entity.ReservationRecordStatus;
import com.server.reservation.reservationrecord.repository.ReservationRecordRepository;
import com.server.reservation.user.dto.RegisterUserRequest;
import com.server.reservation.user.entity.User;
import com.server.reservation.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationRecordServiceModifyTest {
    @Autowired
    UserService userService;
    @Autowired
    ReservationRecordService reservationRecordService;
    @Autowired
    ReservableItemService reservableItemService;
    @Autowired
    ReservableItemRepository reservableItemRepository;
    @Autowired
    ReservationRecordRepository reservationRecordRepository;
    @Autowired
    BusinessService businessService;

    User user;
    Business business;

    ReservationRecord normalReservation;
    ReservationRecord canceledReservation;
    ReservationRecord datePassedReservation;

    ReservableItem normalItem;
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

        ReservableItem datePassedItem = ReservableItem.builder()
                .reservableTime(new Date(System.currentTimeMillis() - 100000000000L))
                .isAvailable(true)
                .price(100L)
                .quantity(2L)
                .maxQuantityPerUser(1L)
                .name("test-item")
                .business(business)
                .build();
        reservableItemRepository.save(datePassedItem);

        normalItem = ReservableItem.builder()
                .reservableTime(new Date(System.currentTimeMillis() + 100000000000L))
                .isAvailable(true)
                .price(100L)
                .quantity(2L)
                .maxQuantityPerUser(1L)
                .name("test-item")
                .business(business)
                .build();
        reservableItemRepository.save(normalItem);

        RegisterReservationRecordRequest registerRequest = new RegisterReservationRecordRequest(
                business.getId(),
                normalItem.getId(),
                1L
        );
        normalReservation = reservationRecordService.registerReservationRecord(
                user.getId(), registerRequest
        );

        canceledReservation = ReservationRecord.builder()
                .user(user)
                .business(business)
                .reservableItem(normalItem)
                .quantity(1L)
                .reservationRecordStatus(ReservationRecordStatus.CANCELED)
                .build();
        reservationRecordRepository.save(canceledReservation);

        datePassedReservation = ReservationRecord.builder()
                .user(user)
                .business(business)
                .reservableItem(datePassedItem)
                .quantity(1L)
                .reservationRecordStatus(ReservationRecordStatus.RESERVED)
                .build();
        reservationRecordRepository.save(datePassedReservation);
    }

    @Test
    @DisplayName("modify - fail - 변경하려는 상태 값이 같음")
    void givenRequest_whenModifySameStatus_thenFailed(){
        // Given
        ModifyReservationRecordRequest registerReservationRecordRequest = new ModifyReservationRecordRequest(
                ReservationRecordStatus.RESERVED
        );

        // When, Then
        CustomException exception = assertThrows(
                CustomException.class, () -> reservationRecordService.modifyReservationRecord(
                        user.getId(), normalReservation.getId(), registerReservationRecordRequest
                )
        );
        assertEquals(ErrorCode.SAME_STATUS.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("modify - fail - 이미 예약 완료, 취소됨")
    void givenRequest_whenModifyAlreadyCanceledOrCompleted_thenFailed(){
        // Given
        ModifyReservationRecordRequest registerReservationRecordRequest = new ModifyReservationRecordRequest(
                ReservationRecordStatus.RESERVED
        );

        // When, Then
        CustomException exception = assertThrows(
                CustomException.class, () -> reservationRecordService.modifyReservationRecord(
                        user.getId(), canceledReservation.getId(), registerReservationRecordRequest
                )
        );
        assertEquals(ErrorCode.ALREADY_CANCELED_OR_COMPLETED.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("modify - fail - 날짜가 지난것 취소 불가.")
    void givenRequest_whenModifyDatePassed_thenFailed(){
        // Given
        ModifyReservationRecordRequest registerReservationRecordRequest = new ModifyReservationRecordRequest(
                ReservationRecordStatus.CANCELED
        );

        // When, Then
        CustomException exception = assertThrows(
                CustomException.class, () -> reservationRecordService.modifyReservationRecord(
                        user.getId(), datePassedReservation.getId(), registerReservationRecordRequest
                )
        );
        assertEquals(ErrorCode.RESERVATION_DATE_PASSED.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("modify - succeed - 성공")
    void givenRequest_whenModifyValidRequest_thenSucceed(){
        // Given
        long balance = user.getBalance();
        long quantity = normalItem.getQuantity();
        ModifyReservationRecordRequest registerReservationRecordRequest = new ModifyReservationRecordRequest(
                ReservationRecordStatus.CANCELED
        );

        // When
        ReservationRecord reservation = reservationRecordService.modifyReservationRecord(
                user.getId(), normalReservation.getId(), registerReservationRecordRequest
        );

        // Then
        assertAll(
                "성공",
                () -> assertEquals(balance + reservation.getQuantity() * normalItem.getPrice(), user.getBalance()),
                () -> assertEquals(quantity + reservation.getQuantity(), normalItem.getQuantity()),
                () -> assertEquals(ReservationRecordStatus.CANCELED, reservation.getReservationRecordStatus())
        );
    }
}