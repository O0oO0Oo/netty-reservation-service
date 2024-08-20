package org.server.rsaga.reservableitem.dto.request;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;
import org.server.rsaga.reservableitem.domain.constant.Unit;

import java.util.Date;

public record ModifyReservableTime(
        @NotNull(message = "id 는 필수 입니다.")
        Long reservableTimeId,

        @NotNull(message = "예약 가능 시간은 필수 입니다.")
        Date reservableTime,

        @NotNull(message = "수량은 필수 입력 값입니다")
        @Range(min = 1L, message = "수량은 1 이상이어야 합니다.")
        Long stockQuantity,

        @NotNull(message = "재고 단위는 필수 입력 값입니다.")
        Unit stockUnit,

        Boolean isTimeAvailable
) {
}
