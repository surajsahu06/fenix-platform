package com.fenix.platform.outbox;

import com.fenix.platform.entity.OutboxEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingOutboxEventPublisher implements OutboxEventPublisher {
    @Override
    public void publish(OutboxEvent event) {
        log.info("Outbox publish: id={} type={} aggregateType={} aggregateId={} payload={}",
                event.getId(), event.getEventType(), event.getAggregateType(), event.getAggregateId(), event.getPayloadJson());
    }
}
