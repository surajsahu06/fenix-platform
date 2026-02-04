package com.fenix.platform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenix.platform.entity.Fulfillment;
import com.fenix.platform.entity.Order;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.entity.OutboxEvent;
import com.fenix.platform.entity.Tracking;
import com.fenix.platform.entity.Website;
import com.fenix.platform.model.OutboxStatus;
import com.fenix.platform.outbox.OutboxAggregateType;
import com.fenix.platform.repository.OutboxEventRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventService {
    private static final int MAX_ERROR_LENGTH = 1000;

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    private final String instanceId = UUID.randomUUID().toString();

    @Transactional
    public void recordOrganizationEvent(String eventType, Organization organization) {
        recordEvent(OutboxAggregateType.ORGANIZATION, organization.getId(), eventType, organizationPayload(organization));
    }

    @Transactional
    public void recordWebsiteEvent(String eventType, Website website) {
        recordEvent(OutboxAggregateType.WEBSITE, website.getId(), eventType, websitePayload(website));
    }

    @Transactional
    public void recordOrderEvent(String eventType, Order order) {
        recordEvent(OutboxAggregateType.ORDER, order.getId(), eventType, orderPayload(order));
    }

    @Transactional
    public void recordFulfillmentEvent(String eventType, Fulfillment fulfillment) {
        recordEvent(OutboxAggregateType.FULFILLMENT, fulfillment.getId(), eventType, fulfillmentPayload(fulfillment));
    }

    @Transactional
    public void recordTrackingEvent(String eventType, Tracking tracking) {
        recordEvent(OutboxAggregateType.TRACKING, tracking.getId(), eventType, trackingPayload(tracking));
    }

    public List<OutboxEvent> loadPending(int batchSize) {
        return repository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, PageRequest.of(0, batchSize)).getContent();
    }

    @Transactional
    public boolean claim(UUID id) {
        int updated = repository.claimEvent(id, OutboxStatus.PENDING, OutboxStatus.IN_PROGRESS,
                OffsetDateTime.now(ZoneOffset.UTC), instanceId);
        return updated == 1;
    }

    @Transactional
    public void markPublished(UUID id) {
        repository.markPublished(id, OutboxStatus.PUBLISHED, OffsetDateTime.now(ZoneOffset.UTC), OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Transactional
    public void markFailed(UUID id, String error) {
        repository.markFailed(id, OutboxStatus.FAILED, truncate(error), OffsetDateTime.now(ZoneOffset.UTC));
    }

    private void recordEvent(String aggregateType, UUID aggregateId, String eventType, Map<String, Object> payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayloadJson(toJson(payload));
        event.setStatus(OutboxStatus.PENDING);
        event.setAttempts(0);
        repository.save(event);
        log.debug("Outbox event recorded: type={} aggregateType={} aggregateId={}", eventType, aggregateType, aggregateId);
    }

    private Map<String, Object> organizationPayload(Organization organization) {
        Map<String, Object> payload = basePayload(organization.getId());
        payload.put("name", organization.getName());
        payload.put("status", organization.getStatus());
        return payload;
    }

    private Map<String, Object> websitePayload(Website website) {
        Map<String, Object> payload = basePayload(website.getId());
        payload.put("orgId", website.getOrganization().getId());
        payload.put("code", website.getCode());
        payload.put("name", website.getName());
        payload.put("platform", website.getPlatform());
        payload.put("status", website.getStatus());
        return payload;
    }

    private Map<String, Object> orderPayload(Order order) {
        Map<String, Object> payload = basePayload(order.getId());
        payload.put("orgId", order.getOrganization().getId());
        payload.put("websiteId", order.getWebsite().getId());
        payload.put("externalOrderId", order.getExternalOrderId());
        payload.put("status", order.getStatus());
        payload.put("financialStatus", order.getFinancialStatus());
        payload.put("fulfillmentStatus", order.getFulfillmentStatus());
        payload.put("orderUpdatedAt", order.getOrderUpdatedAt());
        return payload;
    }

    private Map<String, Object> fulfillmentPayload(Fulfillment fulfillment) {
        Map<String, Object> payload = basePayload(fulfillment.getId());
        payload.put("orderId", fulfillment.getOrder().getId());
        payload.put("orgId", fulfillment.getOrganization().getId());
        payload.put("externalFulfillmentId", fulfillment.getExternalFulfillmentId());
        payload.put("status", fulfillment.getStatus());
        return payload;
    }

    private Map<String, Object> trackingPayload(Tracking tracking) {
        Map<String, Object> payload = basePayload(tracking.getId());
        payload.put("fulfillmentId", tracking.getFulfillment().getId());
        payload.put("orgId", tracking.getOrganization().getId());
        payload.put("trackingNumber", tracking.getTrackingNumber());
        payload.put("status", tracking.getStatus());
        payload.put("isPrimary", tracking.isPrimary());
        return payload;
    }

    private Map<String, Object> basePayload(UUID id) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", id);
        payload.put("occurredAt", OffsetDateTime.now(ZoneOffset.UTC));
        return payload;
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize outbox payload", ex);
        }
    }

    private String truncate(String error) {
        if (error == null) {
            return null;
        }
        if (error.length() <= MAX_ERROR_LENGTH) {
            return error;
        }
        return error.substring(0, MAX_ERROR_LENGTH);
    }
}
