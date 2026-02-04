package com.fenix.platform.service;

import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.dto.WebsiteCreateRequest;
import com.fenix.platform.dto.WebsitePatchRequest;
import com.fenix.platform.dto.WebsiteResponse;
import com.fenix.platform.dto.WebsiteUpdateRequest;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.entity.Website;
import com.fenix.platform.exception.NotFoundException;
import com.fenix.platform.mapper.WebsiteMapper;
import com.fenix.platform.model.Platform;
import com.fenix.platform.model.WebsiteStatus;
import com.fenix.platform.outbox.OutboxEventType;
import com.fenix.platform.repository.OrganizationRepository;
import com.fenix.platform.repository.WebsiteRepository;
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
public class WebsiteService {
    private final WebsiteRepository repository;
    private final OrganizationRepository organizationRepository;
    private final OutboxEventService outboxEventService;

    @Transactional
    public WebsiteResponse create(UUID orgId, WebsiteCreateRequest request) {
        log.info("Creating website orgId={} code={}", orgId, request.getCode());
        Organization organization = getOrganization(orgId);
        Website entity = new Website();
        WebsiteMapper.applyCreate(entity, organization, request);
        Website saved = repository.save(entity);
        outboxEventService.recordWebsiteEvent(OutboxEventType.WEBSITE_CREATED, saved);
        return WebsiteMapper.toResponse(saved);
    }

    public PagedResponse<WebsiteResponse> list(UUID orgId, OffsetDateTime from, OffsetDateTime to, WebsiteStatus status,
                                               Platform platform, String code, Integer page, Integer size, String sort) {
        log.debug("Listing websites orgId={} from={} to={} status={} platform={} code={} page={} size={} sort={}",
                orgId, from, to, status, platform, code, page, size, sort);
        Specification<Website> spec = null;
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("organization.id", orgId));
        spec = SpecificationUtils.and(spec, SpecificationUtils.between("updatedAt", from, to));
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("status", status));
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("platform", platform));
        spec = SpecificationUtils.and(spec, SpecificationUtils.likeIgnoreCase("code", code));
        Pageable pageable = PageableUtils.from(page, size, sort);
        Page<WebsiteResponse> result = repository.findAll(spec, pageable).map(WebsiteMapper::toResponse);
        return PagedResponse.from(result);
    }

    public PagedResponse<WebsiteResponse> search(UUID orgId, UUID websiteId, String code,
                                                 Integer page, Integer size) {
        log.debug("Searching websites orgId={} websiteId={} code={} page={} size={}", orgId, websiteId, code, page, size);
        Specification<Website> spec = null;
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("organization.id", orgId));
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("id", websiteId));
        spec = SpecificationUtils.and(spec, SpecificationUtils.likeIgnoreCase("code", code));
        Pageable pageable = PageableUtils.from(page, size, null);
        Page<WebsiteResponse> result = repository.findAll(spec, pageable).map(WebsiteMapper::toResponse);
        return PagedResponse.from(result);
    }

    public WebsiteResponse get(UUID orgId, UUID websiteId) {
        log.debug("Fetching website orgId={} websiteId={}", orgId, websiteId);
        return WebsiteMapper.toResponse(getEntity(orgId, websiteId));
    }

    @Transactional
    public WebsiteResponse update(UUID orgId, UUID websiteId, WebsiteUpdateRequest request) {
        log.info("Updating website orgId={} websiteId={}", orgId, websiteId);
        Website entity = getEntity(orgId, websiteId);
        WebsiteMapper.applyUpdate(entity, request);
        Website saved = repository.save(entity);
        outboxEventService.recordWebsiteEvent(OutboxEventType.WEBSITE_UPDATED, saved);
        return WebsiteMapper.toResponse(saved);
    }

    @Transactional
    public WebsiteResponse patch(UUID orgId, UUID websiteId, WebsitePatchRequest request) {
        log.info("Patching website orgId={} websiteId={}", orgId, websiteId);
        Website entity = getEntity(orgId, websiteId);
        WebsiteMapper.applyPatch(entity, request);
        Website saved = repository.save(entity);
        outboxEventService.recordWebsiteEvent(OutboxEventType.WEBSITE_PATCHED, saved);
        return WebsiteMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID orgId, UUID websiteId) {
        log.info("Deleting website orgId={} websiteId={}", orgId, websiteId);
        Website entity = getEntity(orgId, websiteId);
        outboxEventService.recordWebsiteEvent(OutboxEventType.WEBSITE_DELETED, entity);
        repository.delete(entity);
    }

    private Website getEntity(UUID orgId, UUID websiteId) {
        Specification<Website> spec = null;
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("organization.id", orgId));
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("id", websiteId));
        return repository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Website not found"));
    }

    private Organization getOrganization(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }
}
