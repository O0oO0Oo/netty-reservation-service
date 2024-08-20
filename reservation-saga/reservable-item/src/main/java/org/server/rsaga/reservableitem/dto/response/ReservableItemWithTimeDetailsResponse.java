package org.server.rsaga.reservableitem.dto.response;

import org.server.rsaga.reservableitem.domain.ReservableItem;
import org.server.rsaga.reservableitem.domain.ReservableTime;
import org.server.rsaga.reservableitem.domain.Stock;

import java.util.ArrayList;
import java.util.List;

public record ReservableItemWithTimeDetailsResponse(
        Long reservableItemId,
        String name,
        Long maxQuantityPerUser,
        Long price,
        Long businessId,
        boolean isItemAvailable,
        List<ReservableTimeDetailsDto> reservableTimes
) {
    public static ReservableItemWithTimeDetailsResponse of(ReservableItem reservableItem
    ) {
        List<ReservableTime> reservableTimeList = reservableItem.getReservableTimes();
        List<ReservableTimeDetailsDto> reservableTimeDetailsResponse = new ArrayList<>();

        for (ReservableTime reservableTime : reservableTimeList) {
            Stock stock = reservableTime.getStock();

            ReservableTimeDetailsDto reservableTimeDetailsDto = new ReservableTimeDetailsDto(
                    reservableTime.getId(),
                    stock.getQuantity(),
                    stock.getUnit(),
                    reservableTime.getTime(),
                    reservableTime.isTimeAvailable()
            );

            reservableTimeDetailsResponse.add(reservableTimeDetailsDto);
        }

        return new ReservableItemWithTimeDetailsResponse(
                reservableItem.getId(),
                reservableItem.getName(),
                reservableItem.getMaxQuantityPerUser(),
                reservableItem.getPrice(),
                reservableItem.getBusinessId(),
                reservableItem.isItemAvailable(),
                reservableTimeDetailsResponse
        );
    }

    public static List<ReservableItemWithTimeDetailsResponse> of(List<ReservableItem> reservableItems) {
        List<ReservableItemWithTimeDetailsResponse> responses = new ArrayList<>();
        for (ReservableItem reservableItem : reservableItems) {
            ReservableItemWithTimeDetailsResponse response = of(reservableItem);
            responses.add(response);
        }
        return responses;
    }
}
