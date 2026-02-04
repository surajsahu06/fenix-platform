package com.fenix.platform.outbox;

import com.fenix.platform.entity.OutboxEvent;
import com.fenix.platform.service.OutboxEventService;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPollingWorker {
    private final OutboxEventService outboxEventService;
    private final OutboxEventPublisher publisher;

    @Value("${outbox.polling.enabled:true}")
    private boolean enabled;

    @Value("${outbox.polling.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${outbox.polling.fixed-delay:2000}",
            initialDelayString = "${outbox.polling.initial-delay:1000}")
    public void poll() {
        if (!enabled) {
            return;
        }
        List<OutboxEvent> events = outboxEventService.loadPending(batchSize);
        if (events.isEmpty()) {
            return;
        }
        for (OutboxEvent event : events) {
            process(event);
        }
    }

    private void process(OutboxEvent event) {
        if (!outboxEventService.claim(event.getId())) {
            return;
        }
        try {
            publisher.publish(event);
            outboxEventService.markPublished(event.getId());
        } catch (Exception ex) {
            log.error("Outbox publish failed id={} type={}", event.getId(), event.getEventType(), ex);
            outboxEventService.markFailed(event.getId(), ex.getMessage());
        }
    }
}
