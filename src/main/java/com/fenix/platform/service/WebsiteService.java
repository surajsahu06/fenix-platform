package com.fenix.platform.service;

import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.dto.WebsiteCreateRequest;
import com.fenix.platform.dto.WebsitePatchRequest;
import com.fenix.platform.dto.WebsiteResponse;
import com.fenix.platform.dto.WebsiteUpdateRequest;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.entity.Website;
import com.fenix.platform.mapper.WebsiteMapper;
import com.fenix.platform.model.Platform;
import com.fenix.platform.model.WebsiteStatus;
import com.fenix.platform.outbox.OutboxEventType;
import com.fenix.platform.repository.WebsiteRepository;
import com.fenix.platform.tenant.TenantAccessGuard;
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
public class WebsiteService {
    private final WebsiteRepository repository;
    private final OutboxEventService outboxEventService;
    private final PageableFactory pageableFactory;
    private final TenantAccessGuard tenantAccessGuard;

    @Transactional
    public WebsiteResponse create(UUID orgId, WebsiteCreateRequest request) {
        log.info("Creating website orgId={} code={}", orgId, request.getCode());
        Organization organization = tenantAccessGuard.requireOrganization(orgId);
        Website entity = new Website();
        WebsiteMapper.applyCreate(entity, organization, request);
        Website saved = repository.save(entity);
        outboxEventService.recordWebsiteEvent(OutboxEventType.WEBSITE_CREATED, saved);
        return WebsiteMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
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
        Pageable pageable = pageableFactory.from(page, size, sort);
        Page<WebsiteResponse> result = repository.findAll(spec, pageable).map(WebsiteMapper::toResponse);
        return PagedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WebsiteResponse> search(UUID orgId, UUID websiteId, String code,
                                                 Integer page, Integer size) {
        log.debug("Searching websites orgId={} websiteId={} code={} page={} size={}", orgId, websiteId, code, page, size);
        if (websiteId != null) {
            tenantAccessGuard.ensureWebsiteInOrganization(orgId, websiteId);
        }
        Specification<Website> spec = null;
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("organization.id", orgId));
        spec = SpecificationUtils.and(spec, SpecificationUtils.equal("id", websiteId));
        spec = SpecificationUtils.and(spec, SpecificationUtils.likeIgnoreCase("code", code));
        Pageable pageable = pageableFactory.from(page, size, null);
        Page<WebsiteResponse> result = repository.findAll(spec, pageable).map(WebsiteMapper::toResponse);
        return PagedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public WebsiteResponse get(UUID orgId, UUID websiteId) {
        log.debug("Fetching website orgId={} websiteId={}", orgId, websiteId);
        return WebsiteMapper.toResponse(tenantAccessGuard.requireWebsite(orgId, websiteId));
    }

    @Transactional
    public WebsiteResponse update(UUID orgId, UUID websiteId, WebsiteUpdateRequest request) {
        log.info("Updating website orgId={} websiteId={}", orgId, websiteId);
        Website entity = tenantAccessGuard.requireWebsite(orgId, websiteId);
        WebsiteMapper.applyUpdate(entity, request);
        Website saved = repository.save(entity);
        outboxEventService.recordWebsiteEvent(OutboxEventType.WEBSITE_UPDATED, saved);
        return WebsiteMapper.toResponse(saved);
    }

    @Transactional
    public WebsiteResponse patch(UUID orgId, UUID websiteId, WebsitePatchRequest request) {
        log.info("Patching website orgId={} websiteId={}", orgId, websiteId);
        Website entity = tenantAccessGuard.requireWebsite(orgId, websiteId);
        WebsiteMapper.applyPatch(entity, request);
        Website saved = repository.save(entity);
        outboxEventService.recordWebsiteEvent(OutboxEventType.WEBSITE_PATCHED, saved);
        return WebsiteMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID orgId, UUID websiteId) {
        log.info("Deleting website orgId={} websiteId={}", orgId, websiteId);
        Website entity = tenantAccessGuard.requireWebsite(orgId, websiteId);
        outboxEventService.recordWebsiteEvent(OutboxEventType.WEBSITE_DELETED, entity);
        repository.delete(entity);
    }
}
