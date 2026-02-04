package com.fenix.platform.service;

import com.fenix.platform.dto.FulfillmentCreateRequest;
import com.fenix.platform.dto.FulfillmentResponse;
import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.config.PagingProperties;
import com.fenix.platform.entity.Fulfillment;
import com.fenix.platform.entity.Order;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.exception.NotFoundException;
import com.fenix.platform.model.FulfillmentStatus;
import com.fenix.platform.repository.FulfillmentRepository;
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
class FulfillmentServiceTest {
    @Mock
    private FulfillmentRepository repository;

    @Mock
    private OrderService orderService;

    @Mock
    private OutboxEventService outboxEventService;

    private FulfillmentService service;
    private PageableFactory pageableFactory;

    @BeforeEach
    void setUp() {
        pageableFactory = new PageableFactory(new PagingProperties());
        service = new FulfillmentService(repository, orderService, outboxEventService, pageableFactory);
    }

    @Test
    void createAssignsOrderAndOrganization() {
        UUID orderId = UUID.randomUUID();
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());

        Order order = new Order();
        order.setId(orderId);
        order.setOrganization(organization);

        FulfillmentCreateRequest request = new FulfillmentCreateRequest();
        request.setExternalFulfillmentId("ful-1");
        request.setStatus(FulfillmentStatus.SHIPPED);

        when(orderService.getEntity(orderId)).thenReturn(order);
        when(repository.save(any(Fulfillment.class))).thenAnswer(invocation -> {
            Fulfillment fulfillment = invocation.getArgument(0);
            if (fulfillment.getId() == null) {
                fulfillment.setId(UUID.randomUUID());
            }
            return fulfillment;
        });

        FulfillmentResponse response = service.create(orderId, request);

        ArgumentCaptor<Fulfillment> captor = ArgumentCaptor.forClass(Fulfillment.class);
        verify(repository).save(captor.capture());
        Fulfillment saved = captor.getValue();

        assertThat(saved.getOrder()).isEqualTo(order);
        assertThat(saved.getOrganization()).isEqualTo(organization);
        assertThat(response.getOrderId()).isEqualTo(orderId);
    }

    @Test
    void listUsesDefaultSortWhenMissing() {
        UUID orderId = UUID.randomUUID();
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());

        Order order = new Order();
        order.setId(orderId);
        order.setOrganization(organization);

        Fulfillment fulfillment = new Fulfillment();
        fulfillment.setId(UUID.randomUUID());
        fulfillment.setOrder(order);
        fulfillment.setOrganization(organization);
        fulfillment.setExternalFulfillmentId("ful-2");
        fulfillment.setStatus(FulfillmentStatus.CREATED);
        fulfillment.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        fulfillment.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        PageRequest expected = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "updatedAt"));
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(fulfillment), expected, 1));

        PagedResponse<FulfillmentResponse> response = service.list(orderId, null, null, null, null, null, null, null);

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
        UUID orderId = UUID.randomUUID();
        UUID fulfillmentId = UUID.randomUUID();
        when(repository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(orderId, fulfillmentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Fulfillment not found");
    }
}
