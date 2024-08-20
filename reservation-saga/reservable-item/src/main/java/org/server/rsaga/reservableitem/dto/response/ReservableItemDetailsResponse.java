package org.server.rsaga.reservableitem.dto.response;

import org.server.rsaga.reservableitem.domain.ReservableItem;

import java.util.ArrayList;
import java.util.List;

public record ReservableItemDetailsResponse(
        Long reservableItemId,
        String name,
        Long maxQuantityPerUser,
        Long price,
        Long businessId,
        boolean isItemAvailable
) {
    public static ReservableItemDetailsResponse of(ReservableItem reservableItem
    ) {
        return new ReservableItemDetailsResponse(
                reservableItem.getId(),
                reservableItem.getName(),
                reservableItem.getMaxQuantityPerUser(),
                reservableItem.getPrice(),
                reservableItem.getBusinessId(),
                reservableItem.isItemAvailable()
        );
    }

    public static List<ReservableItemDetailsResponse> of(List<ReservableItem> reservableItems) {
        List<ReservableItemDetailsResponse> responses = new ArrayList<>();
        for (ReservableItem reservableItem : reservableItems) {
            responses.add(
                    of(reservableItem)
            );
        }

        return responses;
    }
}

