package com.server.reservation.business.controller;

import com.server.reservation.business.dto.ModifyBusinessRequest;
import com.server.reservation.business.dto.RegisterBusinessRequest;
import com.server.reservation.business.service.BusinessService;
import com.server.reservation.common.dto.FullHttpResponseBuilder;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/businesses")
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