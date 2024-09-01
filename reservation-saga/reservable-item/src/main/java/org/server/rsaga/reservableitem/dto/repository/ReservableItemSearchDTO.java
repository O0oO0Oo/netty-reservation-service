package org.server.rsaga.reservableitem.dto.repository;

import org.server.rsaga.common.domain.ForeignKey;

import java.util.Objects;

public record ReservableItemSearchDTO(
        Long reservableItemId,
        ForeignKey businessId,
        Long reservableTimeId
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservableItemSearchDTO that = (ReservableItemSearchDTO) o;
        return reservableItemId.equals(that.reservableItemId) && businessId.equals(that.businessId) && reservableTimeId.equals(that.reservableTimeId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(reservableItemId, businessId, reservableTimeId);
    }
}
