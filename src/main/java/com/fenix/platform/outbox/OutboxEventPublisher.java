package com.fenix.platform.outbox;

import com.fenix.platform.entity.OutboxEvent;

public interface OutboxEventPublisher {
    void publish(OutboxEvent event);
}
