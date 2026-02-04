package com.fenix.platform.service;

import com.fenix.platform.dto.OrderCreateRequest;
import com.fenix.platform.dto.OrderPatchRequest;
import com.fenix.platform.dto.OrderResponse;
import com.fenix.platform.dto.OrderUpdateRequest;
import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.entity.Order;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.entity.Website;
import com.fenix.platform.exception.NotFoundException;
import com.fenix.platform.mapper.OrderMapper;
import com.fenix.platform.model.FinancialStatus;
import com.fenix.platform.model.FulfillmentOverallStatus;
import com.fenix.platform.model.OrderStatus;
import com.fenix.platform.outbox.OutboxEventType;
import com.fenix.platform.repository.OrderRepository;
import com.fenix.platform.tenant.TenantAccessGuard;
import com.fenix.platform.util.PageableFactory;
import com.fenix.platform.util.SpecificationBuilder;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository repository;
    private final OutboxEventService outboxEventService;
    private final PageableFactory pageableFactory;
    private final TenantAccessGuard tenantAccessGuard;

    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        log.info("Creating order orgId={} websiteId={} externalOrderId={}",
                request.getOrgId(), request.getWebsiteId(), request.getExternalOrderId());
        Organization organization = tenantAccessGuard.requireOrganization(request.getOrgId());
        Website website = tenantAccessGuard.requireWebsite(request.getOrgId(), request.getWebsiteId());
        Order entity = repository
                .findByOrganizationIdAndWebsiteIdAndExternalOrderId(organization.getId(), website.getId(), request.getExternalOrderId())
                .orElseGet(Order::new);
        boolean isNew = entity.getId() == null;
        OrderMapper.applyCreate(entity, organization, website, request);
        Order saved = repository.save(entity);
        outboxEventService.recordOrderEvent(isNew ? OutboxEventType.ORDER_CREATED : OutboxEventType.ORDER_UPDATED, saved);
        return OrderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> list(UUID orgId, UUID websiteId, OffsetDateTime from, OffsetDateTime to,
                                             OrderStatus status, FinancialStatus financialStatus, FulfillmentOverallStatus fulfillmentStatus,
                                             Integer page, Integer size, String sort) {
        log.debug("Listing orders orgId={} websiteId={} from={} to={} status={} financialStatus={} fulfillmentStatus={} page={} size={} sort={}",
                orgId, websiteId, from, to, status, financialStatus, fulfillmentStatus, page, size, sort);
        if (websiteId != null) {
            tenantAccessGuard.ensureWebsiteInOrganization(orgId, websiteId);
        }
        String resolvedSort = sort != null ? sort : "orderUpdatedAt,desc";
        Specification<Order> spec = SpecificationBuilder.<Order>builder()
                .equal("organization.id", orgId)
                .equal("website.id", websiteId)
                .between("orderUpdatedAt", from, to)
                .equal("status", status)
                .equal("financialStatus", financialStatus)
                .equal("fulfillmentStatus", fulfillmentStatus)
                .build();
        Pageable pageable = pageableFactory.from(page, size, resolvedSort);
        Page<OrderResponse> result = repository.findAll(spec, pageable).map(OrderMapper::toResponse);
        return PagedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> search(UUID orgId, UUID websiteId, String externalOrderId, String externalOrderNumber,
                                               Integer page, Integer size) {
        log.debug("Searching orders orgId={} websiteId={} externalOrderId={} externalOrderNumber={} page={} size={}",
                orgId, websiteId, externalOrderId, externalOrderNumber, page, size);
        if (websiteId != null) {
            tenantAccessGuard.ensureWebsiteInOrganization(orgId, websiteId);
        }
        Specification<Order> spec = SpecificationBuilder.<Order>builder()
                .equal("organization.id", orgId)
                .equal("website.id", websiteId)
                .equal("externalOrderId", externalOrderId)
                .equal("externalOrderNumber", externalOrderNumber)
                .build();
        Pageable pageable = pageableFactory.from(page, size, "orderUpdatedAt,desc");
        Page<OrderResponse> result = repository.findAll(spec, pageable).map(OrderMapper::toResponse);
        return PagedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID orderId) {
        log.debug("Fetching order id={}", orderId);
        return OrderMapper.toResponse(getEntity(orderId));
    }

    @Transactional
    public OrderResponse update(UUID orderId, OrderUpdateRequest request) {
        log.info("Updating order id={}", orderId);
        Order entity = getEntity(orderId);
        Organization organization = tenantAccessGuard.requireOrganization(request.getOrgId());
        Website website = tenantAccessGuard.requireWebsite(request.getOrgId(), request.getWebsiteId());
        OrderMapper.applyUpdate(entity, organization, website, request);
        Order saved = repository.save(entity);
        outboxEventService.recordOrderEvent(OutboxEventType.ORDER_UPDATED, saved);
        return OrderMapper.toResponse(saved);
    }

    @Transactional
    public OrderResponse patch(UUID orderId, OrderPatchRequest request) {
        log.info("Patching order id={}", orderId);
        Order entity = getEntity(orderId);
        OrderMapper.applyPatch(entity, request);
        Order saved = repository.save(entity);
        outboxEventService.recordOrderEvent(OutboxEventType.ORDER_PATCHED, saved);
        return OrderMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID orderId) {
        log.info("Deleting order id={}", orderId);
        Order entity = getEntity(orderId);
        outboxEventService.recordOrderEvent(OutboxEventType.ORDER_DELETED, entity);
        repository.delete(entity);
    }

    public Order getEntity(UUID orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }
}
