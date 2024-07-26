package org.server.rsaga.reservableitem.controller;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.dto.FullHttpResponseBuilder;
import org.server.rsaga.reservableitem.dto.ModifyItemRequest;
import org.server.rsaga.reservableitem.dto.RegisterItemRequest;
import org.server.rsaga.reservableitem.service.ItemService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    // 특정 회사 아이템 리스트 조회
    @GetMapping("/{business_id}/item")
    public FullHttpResponse findBusinessReservableItemList(@PathVariable("business_id") Long businessId) {
        return FullHttpResponseBuilder.builder()
                .body(
                        itemService.findBusinessReservableItemList(businessId)
                )
                .build();
    }

    // TODO : 페이징 설정
    @GetMapping("/item")
    public FullHttpResponse findReservableItemList(){
        return FullHttpResponseBuilder.builder()
                .body(
                        itemService.findReservableItemList()
                )
                .build();
    }

    @GetMapping("/{business_id}/item/{item_id}")
    public FullHttpResponse findBusinessReservableItem(@PathVariable("item_id") Long itemId, @PathVariable("business_id") Long businessId){
        return FullHttpResponseBuilder.builder()
                .body(
                        itemService.findBusinessReservableItem(itemId, businessId)
                )
                .build();
    }

    @PostMapping("/{business_id}/item")
    public FullHttpResponse registerBusinessReservableItem(@PathVariable("business_id") Long businessId, @RequestBody RegisterItemRequest request){
        return FullHttpResponseBuilder.builder()
                .body(
                        itemService.registerBusinessReservableItem(businessId, request)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @PutMapping("/{business_id}/item/{item_id}")
    public FullHttpResponse modifyBusinessReservableItem( @PathVariable("item_id") Long itemId, @PathVariable("business_id") Long businessId, @RequestBody ModifyItemRequest request){
        return FullHttpResponseBuilder.builder()
                .body(
                        itemService.modifyBusinessReservableItem(itemId, businessId, request)
                )
                .build();
    }

    @DeleteMapping("/{business_id}/item/{item_id}")
    public FullHttpResponse deleteBusinessReservableItem(@PathVariable("item_id") Long itemId, @PathVariable("business_id") Long businessId){
        itemService.deleteBusinessReservableItem(itemId, businessId);
        return FullHttpResponseBuilder.builder()
                .body("Deleted.")
                .build();
    }
}
