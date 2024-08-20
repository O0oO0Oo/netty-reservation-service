package org.server.rsaga.common.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher implements ApplicationEventPublisherAware {
    private static ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        EventPublisher.publisher = publisher;
    }

    public static void publish(Object event) {
        if (publisher != null) {
            publisher.publishEvent(event);
        } else {
            throw new IllegalStateException("ApplicationEventPublisher is not initialized.");
        }
    }
}