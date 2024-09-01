package org.server.rsaga.reservation.dto.repository;

import org.server.rsaga.common.domain.ForeignKey;

import java.util.Objects;

public record UserItemPairDto(
        ForeignKey userId,
        ForeignKey reservableItemId
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserItemPairDto that = (UserItemPairDto) o;
        return userId.equals(that.userId) && reservableItemId.equals(that.reservableItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, reservableItemId);
    }
}
