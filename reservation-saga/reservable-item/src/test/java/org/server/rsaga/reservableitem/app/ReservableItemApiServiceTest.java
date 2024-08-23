package org.server.rsaga.reservableitem.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.common.event.BusinessValidationEvent;
import org.server.rsaga.common.event.EventPublisher;
import org.server.rsaga.reservableitem.domain.ReservableItem;
import org.server.rsaga.reservableitem.domain.ReservableTime;
import org.server.rsaga.reservableitem.domain.Stock;
import org.server.rsaga.reservableitem.domain.constant.Unit;
import org.server.rsaga.reservableitem.dto.request.*;
import org.server.rsaga.reservableitem.dto.response.ReservableItemWithTimeDetailsResponse;
import org.server.rsaga.reservableitem.dto.response.ReservableTimeDetailsDto;
import org.server.rsaga.reservableitem.infra.repository.ReservableItemCustomRepository;
import org.server.rsaga.reservableitem.infra.repository.ReservableItemJpaRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservableItemApiService tests")
class ReservableItemApiServiceTest {
    @Mock
    ReservableItemJpaRepository reservableItemJpaRepository;
    @Mock
    ReservableItemCustomRepository reservableItemCustomRepository;

    @Mock
    ReservableItem reservableItem;

    @InjectMocks
    ReservableItemApiService reservableItemApiService;

    @Test
    @DisplayName("registerReservableItem() - valid request - succeed")
    void should_succeed_when_registerReservableItemAndValidRequest() {
        try (MockedStatic<EventPublisher> eventPublisherMockedStatic = mockStatic(EventPublisher.class)) {
            // given
            List<ReservableTime> reservableTimes;
            Stock stock = mock(Stock.class);

            // static method
            Long businessId = 1L;
            BusinessValidationEvent businessValidationEvent = new BusinessValidationEvent(businessId);
            eventPublisherMockedStatic
                    .when(
                            () -> EventPublisher.publish(businessValidationEvent)
                    )
                    .thenAnswer(invocation -> null);

            // registerReservableTimes
            Date date = new Date(System.currentTimeMillis() + 3600 * 1000);
            Long stockQuantity = 100L;
            Unit unit = Unit.BOX;
            List<RegisterReservableTime> registerReservableTimes = new ArrayList<>();
            registerReservableTimes.add(
                    new RegisterReservableTime(
                            date,
                            stockQuantity,
                            unit,
                            true
                    )
            );

            // registerReservableItemRequest
            String itemName = "test item";
            Long maxQuantityPerUser = 10L;
            Long price = 1000L;
            RegisterReservableItemRequest registerReservableItemRequest = new RegisterReservableItemRequest(
                    itemName,
                    maxQuantityPerUser,
                    price,
                    businessId,
                    true,
                    registerReservableTimes
            );

            reservableTimes = new ArrayList<>();
            ReservableTime reservableTime = mock(ReservableTime.class);
            reservableTimes.add(reservableTime);

            when(reservableItemJpaRepository.save(any())).thenReturn(reservableItem);

            /**
             * {@link ReservableItemWithTimeDetailsResponse} 의 of 메서드
             */
            when(reservableItem.getReservableTimes()).thenReturn(reservableTimes);

            when(reservableTime.getStock()).thenReturn(stock);

            when(stock.getQuantity()).thenReturn(stockQuantity);
            when(stock.getUnit()).thenReturn(unit);
            when(reservableTime.getTime()).thenReturn(date);

            when(reservableItem.getName()).thenReturn(itemName);
            when(reservableItem.getMaxQuantityPerUser()).thenReturn(maxQuantityPerUser);
            when(reservableItem.getPrice()).thenReturn(price);
            when(reservableItem.getBusinessId()).thenReturn(businessId);

            // when
            ReservableItemWithTimeDetailsResponse response = reservableItemApiService.registerReservableItem(registerReservableItemRequest);

            // then
            List<ReservableTimeDetailsDto> reservableTimeDetailsDtos = response.reservableTimes();
            for (ReservableTimeDetailsDto reservableTimeDetailsDto : reservableTimeDetailsDtos) {
                assertEquals(stockQuantity, reservableTimeDetailsDto.quantity(), "The stock quantity should be " + stockQuantity);
                assertEquals(unit, reservableTimeDetailsDto.unit(), "The stock unit should be " + unit.name());
                assertEquals(date, reservableTimeDetailsDto.reservableTime(), "The reservableTime date should be " + date);
            }

            assertEquals(itemName, response.name(), "The item name should be " + itemName);
            assertEquals(maxQuantityPerUser, response.maxQuantityPerUser(), "The maxQuantityPerUser should be " + maxQuantityPerUser);
            assertEquals(price, response.price(), "The price should be " + price);
            assertEquals(businessId, response.businessId(), "The businessId should be " + businessId);
        }
    }


    @Test
    @DisplayName("modifyReservableItem() - valid request - succeed")
    void should_succeed_when_modifyReservableItemAndValidRequest() {
        // given
        Long reservableItemId = 1L;
        String newName = "new name";
        Long newMaxQuantityPerUser = 1L;
        Long newPrice = 1L;
        Money newMoneyPrice = new Money(newPrice);
        Long businessId = 1L;

        Long reservableTimeId = 1L;
        Date newDate = new Date(System.currentTimeMillis() + 3600 * 1000);
        Long newStockQuantity = 10L;
        Unit newUnit = Unit.BOX;
        Stock newStock = new Stock(newStockQuantity, newUnit);
        ModifyReservableItemRequest request = new ModifyReservableItemRequest(
                newName,
                newMaxQuantityPerUser,
                newPrice,
                businessId,
                true,
                new ModifyReservableTime(
                        reservableTimeId,
                        newDate,
                        newStockQuantity,
                        newUnit,
                        true
                )
        );

        when(reservableItemCustomRepository.findReservableItemByIdAndBusinessIdOrElseThrow(
                        reservableItemId,
                        new ForeignKey(businessId)
                )
        )
                .thenReturn(reservableItem);

        when(reservableItem.changeName(newName)).thenReturn(reservableItem);
        when(reservableItem.changeMaxQuantityPerUser(newMaxQuantityPerUser)).thenReturn(reservableItem);
        when(reservableItem.changePrice(newMoneyPrice)).thenReturn(reservableItem);
        when(reservableItem.changeIsItemAvailable(true)).thenReturn(reservableItem);
        when(reservableItem.updateReservableTime(eq(reservableTimeId), eq(newDate), eq(newStock), eq(true))).thenReturn(reservableItem);

        // when
        reservableItemApiService.modifyReservableItem(reservableItemId, request);

        // then
        verify(reservableItemCustomRepository).findReservableItemByIdAndBusinessIdOrElseThrow(reservableItemId, new ForeignKey(businessId));
        verify(reservableItem).changeName(newName);
        verify(reservableItem).changeMaxQuantityPerUser(newMaxQuantityPerUser);
        verify(reservableItem).changePrice(newMoneyPrice);
        verify(reservableItem).changeIsItemAvailable(true);
        verify(reservableItem).updateReservableTime(reservableTimeId, newDate, newStock, true);
    }

    @Test
    @DisplayName("deleteReservableItem() - valid request - succeed")
    void should_succeed_when_deleteReservableItemAndValidRequest() {
        // given
        Long reservableItemId = 1L;
        Long businessId = 1L;
        DeleteReservableItemRequest request = new DeleteReservableItemRequest(businessId);

        when(reservableItemCustomRepository.findReservableItemByIdAndBusinessIdOrElseThrow(
                reservableItemId,
                new ForeignKey(businessId)
        )).thenReturn(reservableItem);

        // when
        reservableItemApiService.deleteReservableItem(reservableItemId, request);

        // then
        verify(reservableItemCustomRepository).findReservableItemByIdAndBusinessIdOrElseThrow(reservableItemId, new ForeignKey(businessId));
        verify(reservableItem).makeUnavailable();
    }
}