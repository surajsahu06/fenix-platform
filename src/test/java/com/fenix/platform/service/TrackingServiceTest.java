package com.fenix.platform.service;

import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.dto.TrackingCreateRequest;
import com.fenix.platform.dto.TrackingResponse;
import com.fenix.platform.config.PagingProperties;
import com.fenix.platform.entity.Fulfillment;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.entity.Order;
import com.fenix.platform.entity.Tracking;
import com.fenix.platform.exception.NotFoundException;
import com.fenix.platform.model.TrackingStatus;
import com.fenix.platform.repository.TrackingRepository;
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
class TrackingServiceTest {
    @Mock
    private TrackingRepository repository;

    @Mock
    private FulfillmentService fulfillmentService;

    @Mock
    private OutboxEventService outboxEventService;

    private TrackingService service;

    private PageableFactory pageableFactory;

    @BeforeEach
    void setUp() {
        pageableFactory = new PageableFactory(new PagingProperties());
        service = new TrackingService(repository, fulfillmentService, outboxEventService, pageableFactory);
    }

    @Test
    void createAssignsFulfillmentAndOrganization() {
        UUID fulfillmentId = UUID.randomUUID();
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setOrganization(organization);

        Fulfillment fulfillment = new Fulfillment();
        fulfillment.setId(fulfillmentId);
        fulfillment.setOrganization(organization);
        fulfillment.setOrder(order);

        TrackingCreateRequest request = new TrackingCreateRequest();
        request.setTrackingNumber("track-1");
        request.setCarrier("UPS");
        request.setStatus(TrackingStatus.IN_TRANSIT);
        request.setIsPrimary(Boolean.TRUE);

        when(fulfillmentService.getEntity(fulfillmentId)).thenReturn(fulfillment);
        when(repository.save(any(Tracking.class))).thenAnswer(invocation -> {
            Tracking tracking = invocation.getArgument(0);
            if (tracking.getId() == null) {
                tracking.setId(UUID.randomUUID());
            }
            return tracking;
        });

        TrackingResponse response = service.create(fulfillmentId, request);

        ArgumentCaptor<Tracking> captor = ArgumentCaptor.forClass(Tracking.class);
        verify(repository).save(captor.capture());
        Tracking saved = captor.getValue();

        assertThat(saved.getFulfillment()).isEqualTo(fulfillment);
        assertThat(saved.getOrganization()).isEqualTo(organization);
        assertThat(saved.isPrimary()).isTrue();
        assertThat(response.getFulfillmentId()).isEqualTo(fulfillmentId);
    }

    @Test
    void listUsesDefaultSortWhenMissing() {
        UUID fulfillmentId = UUID.randomUUID();
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());

        Fulfillment fulfillment = new Fulfillment();
        fulfillment.setId(fulfillmentId);
        fulfillment.setOrganization(organization);

        Tracking tracking = new Tracking();
        tracking.setId(UUID.randomUUID());
        tracking.setFulfillment(fulfillment);
        tracking.setOrganization(organization);
        tracking.setTrackingNumber("track-2");
        tracking.setStatus(TrackingStatus.UNKNOWN);
        tracking.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        tracking.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        PageRequest expected = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "updatedAt"));
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tracking), expected, 1));

        PagedResponse<TrackingResponse> response = service.list(fulfillmentId, null, null, null, null, null, null, null, null);

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
        UUID fulfillmentId = UUID.randomUUID();
        UUID trackingId = UUID.randomUUID();
        when(repository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(fulfillmentId, trackingId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Tracking not found");
    }
}
