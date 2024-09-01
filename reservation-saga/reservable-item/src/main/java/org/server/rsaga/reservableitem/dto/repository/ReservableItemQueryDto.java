package org.server.rsaga.reservableitem.dto.repository;

import java.util.Objects;

public record ReservableItemQueryDto(
        Long reservableItemId,
        Long reservableTimeId
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservableItemQueryDto that = (ReservableItemQueryDto) o;
        return reservableItemId.equals(that.reservableItemId) && reservableTimeId.equals(that.reservableTimeId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(reservableItemId, reservableTimeId);
    }
}
