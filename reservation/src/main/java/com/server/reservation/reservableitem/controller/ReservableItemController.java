package com.server.reservation.reservableitem.controller;

import com.server.reservation.common.dto.FullHttpResponseBuilder;
import com.server.reservation.reservableitem.dto.ModifyReservableItemRequest;
import com.server.reservation.reservableitem.dto.RegisterReservableItemRequest;
import com.server.reservation.reservableitem.service.ReservableItemService;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
public class ReservableItemController {
    private final ReservableItemService reservableItemService;

    // 특정 회사 아이템 리스트 조회
    @GetMapping("/{business_id}/item")
    public FullHttpResponse findBusinessReservableItemList(@PathVariable("business_id") Long businessId) {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservableItemService.findBusinessReservableItemList(businessId)
                )
                .build();
    }

    // TODO : 페이징 설정
    @GetMapping("/item")
    public FullHttpResponse findReservableItemList(){
        return FullHttpResponseBuilder.builder()
                .body(
                        reservableItemService.findReservableItemList()
                )
                .build();
    }

    @GetMapping("/{business_id}/item/{item_id}")
    public FullHttpResponse findBusinessReservableItem(@PathVariable("item_id") Long itemId, @PathVariable("business_id") Long businessId){
        return FullHttpResponseBuilder.builder()
                .body(
                        reservableItemService.findBusinessReservableItem(itemId, businessId)
                )
                .build();
    }

    @PostMapping("/{business_id}/item")
    public FullHttpResponse registerBusinessReservableItem(@PathVariable("business_id") Long businessId, @RequestBody RegisterReservableItemRequest request){
        return FullHttpResponseBuilder.builder()
                .body(
                        reservableItemService.registerBusinessReservableItem(businessId, request)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @PutMapping("/{business_id}/item/{item_id}")
    public FullHttpResponse modifyBusinessReservableItem( @PathVariable("item_id") Long itemId, @PathVariable("business_id") Long businessId, @RequestBody ModifyReservableItemRequest request){
        return FullHttpResponseBuilder.builder()
                .body(
                        reservableItemService.modifyBusinessReservableItem(itemId, businessId, request)
                )
                .build();
    }

    @DeleteMapping("/{business_id}/item/{item_id}")
    public FullHttpResponse deleteBusinessReservableItem(@PathVariable("item_id") Long itemId, @PathVariable("business_id") Long businessId){
        reservableItemService.deleteBusinessReservableItem(itemId, businessId);
        return FullHttpResponseBuilder.builder()
                .body("Deleted.")
                .build();
    }
}
