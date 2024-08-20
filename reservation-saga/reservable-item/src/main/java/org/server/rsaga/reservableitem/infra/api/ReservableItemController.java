package org.server.rsaga.reservableitem.infra.api;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.dto.FullHttpResponseBuilder;
import org.server.rsaga.reservableitem.dto.request.DeleteReservableItemRequest;
import org.server.rsaga.reservableitem.dto.request.ModifyReservableItemRequest;
import org.server.rsaga.reservableitem.dto.request.RegisterReservableItemRequest;
import org.server.rsaga.reservableitem.app.ReservableItemApiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservable-items")
@RequiredArgsConstructor
public class ReservableItemController {
    private final ReservableItemApiService reservableItemApiService;

    // todo : 페이징
    @GetMapping
    public FullHttpResponse findReservableItemList() {
        return FullHttpResponseBuilder.builder()
                .body(
                        reservableItemApiService.findReservableItemList()
                )
                .build();
    }

    @GetMapping("/{reservableItem_id}")
    public FullHttpResponse findReservableItem(@PathVariable("reservableItem_id") Long reservableItemId){
        return FullHttpResponseBuilder.builder()
                .body(
                        reservableItemApiService.findReservableItem(reservableItemId)
                )
                .build();
    }

    @PostMapping
    public FullHttpResponse registerReservableItem(@RequestBody RegisterReservableItemRequest request){
        return FullHttpResponseBuilder.builder()
                .body(
                        reservableItemApiService.registerReservableItem(request)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @PutMapping("/{reservableItem_id}")
    public FullHttpResponse modifyReservableItem( @PathVariable("reservableItem_id") Long reservableItemId, @RequestBody ModifyReservableItemRequest request){
        return FullHttpResponseBuilder.builder()
                .body(
                        reservableItemApiService.modifyReservableItem(reservableItemId, request)
                )
                .build();
    }

    @DeleteMapping("/{reservableItem_id}")
    public FullHttpResponse deleteReservableItem(@PathVariable("reservableItem_id") Long reservableItemId, @RequestBody DeleteReservableItemRequest request){
        reservableItemApiService.deleteReservableItem(reservableItemId, request);
        return FullHttpResponseBuilder.builder()
                .body("Deleted.")
                .build();
    }
}
