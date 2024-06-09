package com.server.reservation.reservableitem.service;

import com.server.reservation.business.dto.RegisterBusinessRequest;
import com.server.reservation.business.entity.Business;
import com.server.reservation.business.entity.BusinessType;
import com.server.reservation.business.service.BusinessService;
import com.server.reservation.common.exception.CustomException;
import com.server.reservation.common.exception.ErrorCode;
import com.server.reservation.reservableitem.dto.RegisterReservableItemRequest;
import com.server.reservation.reservableitem.entity.ReservableItem;
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
class ReservableItemServiceRegisterTest {
    @Autowired
    ReservableItemService reservableItemService;
    @Autowired
    BusinessService businessService;
    Business business;

    @BeforeEach
    void beforeEach() {
        RegisterBusinessRequest registerBusinessRequest = new RegisterBusinessRequest("test-business", BusinessType.STORE);
        business = businessService.registerBusiness(
                registerBusinessRequest
        );
    }

    @Test
    @DisplayName("register - succeed - 정상 요청 성공")
    public void givenRequest_whenRegisterValidRequest_thenSucceed(){
        // Given
        RegisterReservableItemRequest request =
                new RegisterReservableItemRequest("test-item", 1L, 1L, new Date(System.currentTimeMillis() + 1000000L), 1000L);

        // When
        ReservableItem item = reservableItemService.registerBusinessReservableItem(
                business.getId(), request
        );

        // Then
        assertAll(
                "성공",
                () -> assertEquals(request.name(), item.getName()),
                () -> assertEquals(request.price(), item.getPrice()),
                () -> assertEquals(request.maxQuantityPerUser(), item.getMaxQuantityPerUser()),
                () -> assertEquals(request.reservableTime(), item.getReservableTime()),
                () -> assertEquals(request.quantity(), item.getQuantity())
        );
    }

    @Test
    @DisplayName("register - failed - 예약 날짜가 오늘보다 이전 - 실패")
    public void givenRequest_whenRegisterReservableDatePassed_thenFailed(){
        // Given
        RegisterReservableItemRequest request =
                new RegisterReservableItemRequest("test-item", 1L, 1L, new Date(System.currentTimeMillis() - 1000000L), 1000L);

        // When, Then
        CustomException exception = assertThrows(CustomException.class, () ->
                reservableItemService.registerBusinessReservableItem(
                        business.getId(), request)
        );
        assertEquals(ErrorCode.RESERVATION_DATE_LATER_TODAY.getMessage(), exception.getMessage());
    }
}