package org.server.rsaga.reservableitem.dto.response;

import org.server.rsaga.reservableitem.domain.constant.Unit;

import java.util.Date;

public record ReservableTimeDetailsDto(
        Long reservableTimeId,
        Long quantity,
        Unit unit,
        Date reservableTime,
        boolean isTimeAvailable
) {
}
