package com.server.reservation.reservableitem.service;

import com.server.reservation.business.dto.RegisterBusinessRequest;
import com.server.reservation.business.entity.Business;
import com.server.reservation.business.entity.BusinessType;
import com.server.reservation.business.service.BusinessService;
import com.server.reservation.common.exception.CustomException;
import com.server.reservation.common.exception.ErrorCode;
import com.server.reservation.reservableitem.dto.ModifyReservableItemRequest;
import com.server.reservation.reservableitem.dto.RegisterReservableItemRequest;
import com.server.reservation.reservableitem.entity.ReservableItem;
import com.server.reservation.reservableitem.repository.ReservableItemRepository;
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
class ReservableItemServiceModifyTest {
    @Autowired
    ReservableItemService reservableItemService;
    @Autowired
    ReservableItemRepository reservableItemRepository;
    @Autowired
    BusinessService businessService;
    Business business;
    ReservableItem item;
    ReservableItem soldOutItem;
    @BeforeEach
    void beforeEach() {
        RegisterBusinessRequest registerBusinessRequest = new RegisterBusinessRequest("test-business", BusinessType.STORE);
        business = businessService.registerBusiness(
                registerBusinessRequest
        );

        RegisterReservableItemRequest request =
                new RegisterReservableItemRequest("test-item", 1L, 1L, new Date(System.currentTimeMillis() + 1000000L), 1000L);
        item = reservableItemService.registerBusinessReservableItem(business.getId(), request);

        soldOutItem = ReservableItem.builder()
                .business(business)
                .isAvailable(false)
                .price(1000L)
                .quantity(10L)
                .maxQuantityPerUser(1L)
                .reservableTime(new Date(System.currentTimeMillis()))
                .name("test-item2")
                .build();

        reservableItemRepository.save(soldOutItem);

    }

    @Test
    @DisplayName("modify - succeed - 정상 요청 성공")
    public void givenRequest_whenModifyValidRequest_thenSucceed(){
        // Given
        Long itemId = item.getId();
        Long businessId = business.getId();
        ModifyReservableItemRequest request =
                new ModifyReservableItemRequest("test-item", 10L, 10L, new Date(System.currentTimeMillis() + 100000L), 1000L);

        // When
        ReservableItem item = reservableItemService.modifyBusinessReservableItem(
                itemId, businessId, request
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
    @DisplayName("modify - failed - 수정하려고 하는 날짜가 지났음")
    public void givenRequest_whenModifyDateLaterToday_thenFailed(){
        // Given
        Long itemId = item.getId();
        Long businessId = business.getId();
        ModifyReservableItemRequest request =
                new ModifyReservableItemRequest("test-item", 10L, 10L, new Date(System.currentTimeMillis() - 10000L), 1000L);

        // When, Then
        CustomException exception = assertThrows(CustomException.class, () ->
                reservableItemService.modifyBusinessReservableItem(
                        itemId, businessId, request
                ));
        assertEquals(ErrorCode.RESERVATION_DATE_LATER_TODAY.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("modify - failed - 종료되어 변경 불가능")
    public void givenRequest_whenModifyNotAvailableItem_thenFailed(){
        // Given
        Long itemId = soldOutItem.getId();
        Long businessId = business.getId();
        ModifyReservableItemRequest request =
                new ModifyReservableItemRequest("test-item", 10L, 10L, new Date(System.currentTimeMillis() + 1000L), 1000L);

        // When, Then
        CustomException exception = assertThrows(CustomException.class, () ->
                reservableItemService.modifyBusinessReservableItem(
                        itemId, businessId, request
                ));
        assertEquals(ErrorCode.ITEM_IS_NOT_AVAILABLE.getMessage(), exception.getMessage());
    }
}