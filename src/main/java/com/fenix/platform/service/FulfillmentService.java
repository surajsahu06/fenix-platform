package com.fenix.platform.service;

import com.fenix.platform.dto.FulfillmentCreateRequest;
import com.fenix.platform.dto.FulfillmentPatchRequest;
import com.fenix.platform.dto.FulfillmentResponse;
import com.fenix.platform.dto.FulfillmentUpdateRequest;
import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.entity.Fulfillment;
import com.fenix.platform.entity.Order;
import com.fenix.platform.exception.NotFoundException;
import com.fenix.platform.mapper.FulfillmentMapper;
import com.fenix.platform.model.FulfillmentStatus;
import com.fenix.platform.outbox.OutboxEventType;
import com.fenix.platform.repository.FulfillmentRepository;
import com.fenix.platform.util.PageableUtils;
import com.fenix.platform.util.SpecificationUtils;

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
public class FulfillmentService {
    private final FulfillmentRepository repository;
    private final OrderService orderService;
    private final OutboxEventService outboxEventService;

    @Transactional
    public FulfillmentResponse create(UUID orderId, FulfillmentCreateRequest request) {
        log.info("Creating fulfillment orderId={} externalFulfillmentId={}", orderId, request.getExternalFulfillmentId());
        Order order = orderService.getEntity(orderId);
        Fulfillment entity = new Fulfillment();
        FulfillmentMapper.applyCreate(entity, order, request);
        Fulfillment saved = repository.save(entity);
        outboxEventService.recordFulfillmentEvent(OutboxEventType.FULFILLMENT_CREATED, saved);
        return FulfillmentMapper.toResponse(saved);
    }

    public PagedResponse<FulfillmentResponse> list(UUID orderId, OffsetDateTime from, OffsetDateTime to,
                                                   FulfillmentStatus status, String carrier, Integer page, Integer size, String sort) {
        log.debug("Listing fulfillments orderId={} from={} to={} status={} carrier={} page={} size={} sort={}",
                orderId, from, to, status, carrier, page, size, sort);
        Specification<Fulfillment> spec = null;
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("order.id", orderId));
        spec = SpecificationUtils.and(spec, SpecificationUtils.between("updatedAt", from, to));
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("status", status));
        spec = SpecificationUtils.and(spec, SpecificationUtils.likeIgnoreCase("carrier", carrier));
        Pageable pageable = PageableUtils.from(page, size, sort);
        Page<FulfillmentResponse> result = repository.findAll(spec, pageable).map(FulfillmentMapper::toResponse);
        return PagedResponse.from(result);
    }

    public PagedResponse<FulfillmentResponse> search(UUID orderId, String externalFulfillmentId, Integer page, Integer size) {
        log.debug("Searching fulfillment's orderId={} externalFulfillmentId={} page={} size={}",
                orderId, externalFulfillmentId, page, size);
        Specification<Fulfillment> spec = null;
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("order.id", orderId));
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("externalFulfillmentId", externalFulfillmentId));
        Pageable pageable = PageableUtils.from(page, size, null);
        Page<FulfillmentResponse> result = repository.findAll(spec, pageable).map(FulfillmentMapper::toResponse);
        return PagedResponse.from(result);
    }

    public FulfillmentResponse get(UUID orderId, UUID fulfillmentId) {
        log.debug("Fetching fulfillment orderId={} fulfillmentId={}", orderId, fulfillmentId);
        return FulfillmentMapper.toResponse(getEntity(orderId, fulfillmentId));
    }

    @Transactional
    public FulfillmentResponse update(UUID orderId, UUID fulfillmentId, FulfillmentUpdateRequest request) {
        log.info("Updating fulfillment orderId={} fulfillmentId={}", orderId, fulfillmentId);
        Fulfillment entity = getEntity(orderId, fulfillmentId);
        Order order = orderService.getEntity(orderId);
        FulfillmentMapper.applyUpdate(entity, order, request);
        Fulfillment saved = repository.save(entity);
        outboxEventService.recordFulfillmentEvent(OutboxEventType.FULFILLMENT_UPDATED, saved);
        return FulfillmentMapper.toResponse(saved);
    }

    @Transactional
    public FulfillmentResponse patch(UUID orderId, UUID fulfillmentId, FulfillmentPatchRequest request) {
        log.info("Patching fulfillment orderId={} fulfillmentId={}", orderId, fulfillmentId);
        Fulfillment entity = getEntity(orderId, fulfillmentId);
        FulfillmentMapper.applyPatch(entity, request);
        Fulfillment saved = repository.save(entity);
        outboxEventService.recordFulfillmentEvent(OutboxEventType.FULFILLMENT_PATCHED, saved);
        return FulfillmentMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID orderId, UUID fulfillmentId) {
        log.info("Deleting fulfillment orderId={} fulfillmentId={}", orderId, fulfillmentId);
        Fulfillment entity = getEntity(orderId, fulfillmentId);
        outboxEventService.recordFulfillmentEvent(OutboxEventType.FULFILLMENT_DELETED, entity);
        repository.delete(entity);
    }

    public Fulfillment getEntity(UUID orderId, UUID fulfillmentId) {
        Specification<Fulfillment> spec = null;
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("order.id", orderId));
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("id", fulfillmentId));
        return repository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Fulfillment not found"));
    }

    public Fulfillment getEntity(UUID fulfillmentId) {
        return repository.findById(fulfillmentId)
                .orElseThrow(() -> new NotFoundException("Fulfillment not found"));
    }
}
