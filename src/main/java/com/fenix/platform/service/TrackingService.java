package com.fenix.platform.service;

import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.dto.TrackingCreateRequest;
import com.fenix.platform.dto.TrackingPatchRequest;
import com.fenix.platform.dto.TrackingResponse;
import com.fenix.platform.dto.TrackingUpdateRequest;
import com.fenix.platform.entity.Fulfillment;
import com.fenix.platform.entity.Tracking;
import com.fenix.platform.exception.NotFoundException;
import com.fenix.platform.mapper.TrackingMapper;
import com.fenix.platform.model.TrackingStatus;
import com.fenix.platform.outbox.OutboxEventType;
import com.fenix.platform.repository.TrackingRepository;
import com.fenix.platform.util.PageableFactory;
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
public class TrackingService {
    private final TrackingRepository repository;
    private final FulfillmentService fulfillmentService;
    private final OutboxEventService outboxEventService;
    private final PageableFactory pageableFactory;

    @Transactional
    public TrackingResponse create(UUID fulfillmentId, TrackingCreateRequest request) {
        log.info("Creating tracking fulfillmentId={} trackingNumber={}", fulfillmentId, request.getTrackingNumber());
        Fulfillment fulfillment = fulfillmentService.getEntity(fulfillmentId);
        Tracking entity = new Tracking();
        TrackingMapper.applyCreate(entity, fulfillment, request);
        Tracking saved = repository.save(entity);
        outboxEventService.recordTrackingEvent(OutboxEventType.TRACKING_CREATED, saved);
        return TrackingMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TrackingResponse> list(UUID fulfillmentId, OffsetDateTime from, OffsetDateTime to,
                                                TrackingStatus status, String carrier, String trackingNumber, Integer page, Integer size, String sort) {
        log.debug("Listing tracking fulfillmentId={} from={} to={} status={} carrier={} trackingNumber={} page={} size={} sort={}",
                fulfillmentId, from, to, status, carrier, trackingNumber, page, size, sort);
        Specification<Tracking> spec = null;
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("fulfillment.id", fulfillmentId));
        spec = SpecificationUtils.and(spec, SpecificationUtils.between("updatedAt", from, to));
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("status", status));
        spec = SpecificationUtils.and(spec, SpecificationUtils.likeIgnoreCase("carrier", carrier));
        spec = SpecificationUtils.and(spec, SpecificationUtils.likeIgnoreCase("trackingNumber", trackingNumber));
        Pageable pageable = pageableFactory.from(page, size, sort);
        Page<TrackingResponse> result = repository.findAll(spec, pageable).map(TrackingMapper::toResponse);
        return PagedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TrackingResponse> search(UUID fulfillmentId, String trackingNumber, String carrier,
                                                  Integer page, Integer size) {
        log.debug("Searching tracking fulfillmentId={} trackingNumber={} carrier={} page={} size={}",
                fulfillmentId, trackingNumber, carrier, page, size);
        Specification<Tracking> spec = null;
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("fulfillment.id", fulfillmentId));
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("trackingNumber", trackingNumber));
        spec = SpecificationUtils.and(spec, SpecificationUtils.likeIgnoreCase("carrier", carrier));
        Pageable pageable = pageableFactory.from(page, size, null);
        Page<TrackingResponse> result = repository.findAll(spec, pageable).map(TrackingMapper::toResponse);
        return PagedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public TrackingResponse get(UUID fulfillmentId, UUID trackingId) {
        log.debug("Fetching tracking fulfillmentId={} trackingId={}", fulfillmentId, trackingId);
        return TrackingMapper.toResponse(getEntity(fulfillmentId, trackingId));
    }

    @Transactional
    public TrackingResponse update(UUID fulfillmentId, UUID trackingId, TrackingUpdateRequest request) {
        log.info("Updating tracking fulfillmentId={} trackingId={}", fulfillmentId, trackingId);
        Tracking entity = getEntity(fulfillmentId, trackingId);
        Fulfillment fulfillment = fulfillmentService.getEntity(fulfillmentId);
        TrackingMapper.applyUpdate(entity, fulfillment, request);
        Tracking saved = repository.save(entity);
        outboxEventService.recordTrackingEvent(OutboxEventType.TRACKING_UPDATED, saved);
        return TrackingMapper.toResponse(saved);
    }

    @Transactional
    public TrackingResponse patch(UUID fulfillmentId, UUID trackingId, TrackingPatchRequest request) {
        log.info("Patching tracking fulfillmentId={} trackingId={}", fulfillmentId, trackingId);
        Tracking entity = getEntity(fulfillmentId, trackingId);
        TrackingMapper.applyPatch(entity, request);
        Tracking saved = repository.save(entity);
        outboxEventService.recordTrackingEvent(OutboxEventType.TRACKING_PATCHED, saved);
        return TrackingMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID fulfillmentId, UUID trackingId) {
        log.info("Deleting tracking fulfillmentId={} trackingId={}", fulfillmentId, trackingId);
        Tracking entity = getEntity(fulfillmentId, trackingId);
        outboxEventService.recordTrackingEvent(OutboxEventType.TRACKING_DELETED, entity);
        repository.delete(entity);
    }

    private Tracking getEntity(UUID fulfillmentId, UUID trackingId) {
        Specification<Tracking> spec = null;
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("fulfillment.id", fulfillmentId));
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("id", trackingId));
        return repository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Tracking not found"));
    }
}
