package org.server.reservation.core.common.exception;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

@Getter
public enum  ErrorCode {
    // Common
    BAD_REQUEST_BODY(HttpResponseStatus.BAD_REQUEST, "C001", "잘못된 요청입니다. 다음을 확인하세요"),
    BAD_REQUEST_PATH_VARIABLE(HttpResponseStatus.BAD_REQUEST, "C002", "잘못된 요청입니다. Path Parameter 를 확인하세요"),
    INVALID_JSON(HttpResponseStatus.BAD_REQUEST, "C003", "잘못된 요청입니다. Json 형식을 확인하세요."),


    // Business
    BUSINESS_NOT_FOUND(HttpResponseStatus.NOT_FOUND, "B001", "회사를 찾을 수 없습니다."),


    // User
    USER_NOT_FOUND(HttpResponseStatus.NOT_FOUND, "U001", "유저를 찾을 수 없습니다."),
    USER_NAME_ALREADY_EXIST(HttpResponseStatus.BAD_REQUEST, "U002", "이미 존재하는 이름 입니다."),


    // Reservable item
    ITEM_NOT_FOUND(HttpResponseStatus.NOT_FOUND, "I001", "상품을 찾을 수 없습니다."),
    RESERVATION_DATE_LATER_TODAY(HttpResponseStatus.BAD_REQUEST, "I002", "예약날은 현재 날짜 이후여야 합니다."),
    ITEM_IS_NOT_AVAILABLE(HttpResponseStatus.BAD_REQUEST, "I003", "종료되어 변경 불가능한 상품 입니다."),


    // Reservation record
    RESERVATION_RECORD_NOT_FOUND(HttpResponseStatus.NOT_FOUND, "R001", "기록을 찾을 수 없습니다."),
    QUANTITY_INSUFFICIENT(HttpResponseStatus.BAD_REQUEST, "R002", "남은 수량이 부족합니다."),
    ALREADY_CANCELED_OR_COMPLETED(HttpResponseStatus.BAD_REQUEST, "R003", "이미 취소된 예약 입니다."),
    RESERVATION_DATE_PASSED(HttpResponseStatus.BAD_REQUEST, "R004", "날짜가 지난 상품은 예약이나 상태 변경이 불가능 합니다."),
    SAME_STATUS(HttpResponseStatus.BAD_REQUEST, "R005", "변경하려는 값이 같습니다."),
    MAX_QUANTITY_EXCEEDED(HttpResponseStatus.BAD_REQUEST, "R006", "최대 구매 수량을 초과했습니다."),
    RESERVATION_ITEM_IS_NOT_AVAILABLE(HttpResponseStatus.BAD_REQUEST, "R007", "종료된 상품은 예약 불가능 합니다.")
    ;


    private final HttpResponseStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpResponseStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
