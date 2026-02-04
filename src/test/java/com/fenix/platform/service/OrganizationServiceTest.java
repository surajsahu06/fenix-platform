package com.fenix.platform.service;

import com.fenix.platform.dto.OrganizationCreateRequest;
import com.fenix.platform.dto.OrganizationResponse;
import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.config.PagingProperties;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.exception.NotFoundException;
import com.fenix.platform.model.OrgStatus;
import com.fenix.platform.repository.OrganizationRepository;
import com.fenix.platform.service.OutboxEventService;
import com.fenix.platform.util.PageableFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
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
class OrganizationServiceTest {
    @Mock
    private OrganizationRepository repository;

    @Mock
    private OutboxEventService outboxEventService;

    private OrganizationService service;
    private PageableFactory pageableFactory;

    @BeforeEach
    void setUp() {
        pageableFactory = new PageableFactory(new PagingProperties());
        service = new OrganizationService(repository, outboxEventService, pageableFactory);
    }

    @Test
    void createDefaultsStatusWhenNull() {
        OrganizationCreateRequest request = new OrganizationCreateRequest();
        request.setName("Acme");

        when(repository.save(any(Organization.class))).thenAnswer(invocation -> {
            Organization org = invocation.getArgument(0);
            if (org.getId() == null) {
                org.setId(UUID.randomUUID());
            }
            return org;
        });

        OrganizationResponse response = service.create(request);

        ArgumentCaptor<Organization> captor = ArgumentCaptor.forClass(Organization.class);
        verify(repository).save(captor.capture());
        Organization saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(OrgStatus.ACTIVE);
        assertThat(saved.getName()).isEqualTo("Acme");
        assertThat(response.getStatus()).isEqualTo(OrgStatus.ACTIVE);
        assertThat(response.getName()).isEqualTo("Acme");
    }

    @Test
    void listUsesDefaultPageableWhenSortNotProvided() {
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setName("Acme");
        organization.setStatus(OrgStatus.ACTIVE);
        organization.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        organization.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        PageRequest expected = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "updatedAt"));
        when(repository.findAll(org.mockito.ArgumentMatchers.<Specification<Organization>>isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(organization), expected, 1));

        PagedResponse<OrganizationResponse> response = service.list(null, null, null, null, null, null, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(org.mockito.ArgumentMatchers.<Specification<Organization>>isNull(), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(50);
        assertThat(pageable.getSort().getOrderFor("updatedAt")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("updatedAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(response.getData()).hasSize(1);
    }

    @Test
    void getThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Organization not found");
    }
}
