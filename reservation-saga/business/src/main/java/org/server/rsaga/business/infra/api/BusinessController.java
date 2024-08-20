package org.server.rsaga.business.infra.api;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.business.dto.request.ModifyBusinessRequest;
import org.server.rsaga.business.dto.request.RegisterBusinessRequest;
import org.server.rsaga.business.app.BusinessApiService;
import org.server.rsaga.common.dto.FullHttpResponseBuilder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/businesses")
@RequiredArgsConstructor
public class BusinessController {
    private final BusinessApiService businessApiService;

    @GetMapping("/{business_id}")
    public FullHttpResponse findBusiness(@PathVariable("business_id") Long businessId) {
        return FullHttpResponseBuilder.builder()
                .body(
                        businessApiService.findBusiness(businessId)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @PostMapping
    public FullHttpResponse registerBusiness(@RequestBody RegisterBusinessRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        businessApiService.registerBusiness(request)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @PutMapping("/{business_id}")
    public FullHttpResponse modifyBusiness(@PathVariable("business_id") Long businessId, @RequestBody ModifyBusinessRequest request) {
        return FullHttpResponseBuilder.builder()
                .body(
                        businessApiService.modifyBusiness(businessId, request)
                )
                .statusCode(HttpResponseStatus.OK)
                .build();
    }

    @DeleteMapping("/{business_id}")
    public FullHttpResponse deleteBusiness(@PathVariable("business_id") Long businessId) {
        businessApiService.deleteBusiness(businessId);
        return FullHttpResponseBuilder.builder()
                .body("Deleted")
                .statusCode(HttpResponseStatus.OK)
                .build();
    }
}