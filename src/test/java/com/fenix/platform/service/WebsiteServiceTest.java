package com.fenix.platform.service;

import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.dto.WebsiteCreateRequest;
import com.fenix.platform.dto.WebsiteResponse;
import com.fenix.platform.config.PagingProperties;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.entity.Website;
import com.fenix.platform.exception.NotFoundException;
import com.fenix.platform.model.Platform;
import com.fenix.platform.model.WebsiteStatus;
import com.fenix.platform.repository.WebsiteRepository;
import com.fenix.platform.service.OutboxEventService;
import com.fenix.platform.tenant.TenantAccessGuard;
import com.fenix.platform.util.PageableFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebsiteServiceTest {
    @Mock
    private WebsiteRepository repository;

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private TenantAccessGuard tenantAccessGuard;

    private WebsiteService service;
    private PageableFactory pageableFactory;

    @BeforeEach
    void setUp() {
        pageableFactory = new PageableFactory(new PagingProperties());
        service = new WebsiteService(repository, outboxEventService, pageableFactory, tenantAccessGuard);
    }

    @Test
    void createDefaultsStatusAndAssignsOrganization() {
        UUID orgId = UUID.randomUUID();
        Organization organization = new Organization();
        organization.setId(orgId);

        WebsiteCreateRequest request = new WebsiteCreateRequest();
        request.setCode("store-1");
        request.setName("Store One");
        request.setPlatform(Platform.SHOPIFY);

        when(tenantAccessGuard.requireOrganization(orgId)).thenReturn(organization);
        when(repository.save(any(Website.class))).thenAnswer(invocation -> {
            Website website = invocation.getArgument(0);
            if (website.getId() == null) {
                website.setId(UUID.randomUUID());
            }
            return website;
        });

        WebsiteResponse response = service.create(orgId, request);

        ArgumentCaptor<Website> captor = ArgumentCaptor.forClass(Website.class);
        verify(repository).save(captor.capture());
        Website saved = captor.getValue();

        assertThat(saved.getOrganization()).isEqualTo(organization);
        assertThat(saved.getStatus()).isEqualTo(WebsiteStatus.ACTIVE);
        assertThat(response.getOrgId()).isEqualTo(orgId);
    }

    @Test
    void listUsesDefaultSortWhenMissing() {
        UUID orgId = UUID.randomUUID();
        Organization organization = new Organization();
        organization.setId(orgId);

        Website website = new Website();
        website.setId(UUID.randomUUID());
        website.setOrganization(organization);
        website.setCode("store-1");
        website.setName("Store One");
        website.setPlatform(Platform.SHOPIFY);
        website.setStatus(WebsiteStatus.ACTIVE);
        website.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        website.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        PageRequest expected = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "updatedAt"));
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(website), expected, 1));

        PagedResponse<WebsiteResponse> response = service.list(orgId, null, null, null, null, null, null, null, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(50);
        assertThat(pageable.getSort().getOrderFor("updatedAt")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("updatedAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(response.getData()).hasSize(1);
    }

    @Test
    void getThrowsWhenMissing() {
        UUID orgId = UUID.randomUUID();
        UUID websiteId = UUID.randomUUID();
        when(tenantAccessGuard.requireWebsite(orgId, websiteId))
                .thenThrow(new NotFoundException("Website not found"));

        assertThatThrownBy(() -> service.get(orgId, websiteId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Website not found");
    }
}
