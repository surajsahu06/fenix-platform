package com.fenix.platform.service;

import com.fenix.platform.dto.OrganizationCreateRequest;
import com.fenix.platform.dto.OrganizationPatchRequest;
import com.fenix.platform.dto.OrganizationResponse;
import com.fenix.platform.dto.OrganizationUpdateRequest;
import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.exception.NotFoundException;
import com.fenix.platform.mapper.OrganizationMapper;
import com.fenix.platform.model.OrgStatus;
import com.fenix.platform.outbox.OutboxEventType;
import com.fenix.platform.repository.OrganizationRepository;
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
public class OrganizationService {
    private final OrganizationRepository repository;
    private final OutboxEventService outboxEventService;

    @Transactional
    public OrganizationResponse create(OrganizationCreateRequest request) {
        log.info("Creating organization name={}", request.getName());
        Organization entity = new Organization();
        OrganizationMapper.applyCreate(entity, request);
        Organization saved = repository.save(entity);
        outboxEventService.recordOrganizationEvent(OutboxEventType.ORGANIZATION_CREATED, saved);
        return OrganizationMapper.toResponse(saved);
    }

    public PagedResponse<OrganizationResponse> list(OffsetDateTime from, OffsetDateTime to, OrgStatus status, String name,
                                                    Integer page, Integer size, String sort) {
        log.debug("Listing organizations from={} to={} status={} name={} page={} size={} sort={}",
                from, to, status, name, page, size, sort);
        Specification<Organization> spec = null;
        spec = SpecificationUtils.and(spec, SpecificationUtils.between("updatedAt", from, to));
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("status", status));
        spec = SpecificationUtils.and(spec, SpecificationUtils.likeIgnoreCase("name", name));
        Pageable pageable = PageableUtils.from(page, size, sort);
        Page<OrganizationResponse> result = repository.findAll(spec, pageable).map(OrganizationMapper::toResponse);
        return PagedResponse.from(result);
    }

    public OrganizationResponse get(UUID id) {
        log.debug("Fetching organization id={}", id);
        return OrganizationMapper.toResponse(getEntity(id));
    }

    @Transactional
    public OrganizationResponse update(UUID id, OrganizationUpdateRequest request) {
        log.info("Updating organization id={}", id);
        Organization entity = getEntity(id);
        OrganizationMapper.applyUpdate(entity, request);
        Organization saved = repository.save(entity);
        outboxEventService.recordOrganizationEvent(OutboxEventType.ORGANIZATION_UPDATED, saved);
        return OrganizationMapper.toResponse(saved);
    }

    @Transactional
    public OrganizationResponse patch(UUID id, OrganizationPatchRequest request) {
        log.info("Patching organization id={}", id);
        Organization entity = getEntity(id);
        OrganizationMapper.applyPatch(entity, request);
        Organization saved = repository.save(entity);
        outboxEventService.recordOrganizationEvent(OutboxEventType.ORGANIZATION_PATCHED, saved);
        return OrganizationMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting organization id={}", id);
        Organization entity = getEntity(id);
        outboxEventService.recordOrganizationEvent(OutboxEventType.ORGANIZATION_DELETED, entity);
        repository.delete(entity);
    }

    private Organization getEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }
}
