package org.server.rsaga.common.event;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;

public class BusinessValidationEvent {
    private final Long businessId;

    public BusinessValidationEvent(Long businessId) {
        this.businessId = businessId;
    }

    public Long getBusinessId() {
        return businessId;
    }
}