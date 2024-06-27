package org.server.reservation.business.controller;

import org.server.reservation.core.common.dto.FullHttpResponseBuilder;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import org.server.reservation.business.dto.ModifyBusinessRequest;
import org.server.reservation.business.dto.RegisterBusinessRequest;
import org.server.reservation.business.service.BusinessService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
public class BusinessController {
    private final BusinessService businessService;

    @GetMapping("/{business_id}")
    public FullHttpResponse findBusiness(@PathVariable("business_id") Long businessId) {
        return FullHttpResponseBuilder.builder()
                .body(
                        businessService.findBusiness(businessId)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @PostMapping
    public FullHttpResponse registerBusiness(@RequestBody RegisterBusinessRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        businessService.registerBusiness(request)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @PutMapping("/{business_id}")
    public FullHttpResponse modifyBusiness(@PathVariable("business_id") Long businessId, @RequestBody ModifyBusinessRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        businessService.modifyBusiness(businessId, request)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @DeleteMapping("/{business_id}")
    public FullHttpResponse deleteBusiness(@PathVariable("business_id") Long businessId) {
        businessService.deleteBusiness(businessId);
        return FullHttpResponseBuilder.builder()
                .body("Deleted")
                .statusCode(HttpResponseStatus.OK)
                .build();
    }
}