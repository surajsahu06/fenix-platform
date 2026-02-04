package com.fenix.platform.service;

import com.fenix.platform.dto.OrderCreateRequest;
import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.dto.OrderResponse;
import com.fenix.platform.entity.Order;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.entity.Website;
import com.fenix.platform.exception.NotFoundException;
import com.fenix.platform.model.FinancialStatus;
import com.fenix.platform.model.FulfillmentOverallStatus;
import com.fenix.platform.model.OrderStatus;
import com.fenix.platform.model.Platform;
import com.fenix.platform.model.WebsiteStatus;
import com.fenix.platform.repository.OrderRepository;
import com.fenix.platform.repository.OrganizationRepository;
import com.fenix.platform.repository.WebsiteRepository;
import com.fenix.platform.service.OutboxEventService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository repository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private WebsiteRepository websiteRepository;

    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private OrderService service;

    @Test
    void createReusesExistingOrder() {
        UUID orgId = UUID.randomUUID();
        UUID websiteId = UUID.randomUUID();
        String externalOrderId = "ext-order-1";

        Organization organization = new Organization();
        organization.setId(orgId);

        Website website = new Website();
        website.setId(websiteId);
        website.setOrganization(organization);
        website.setCode("store-1");
        website.setName("Store One");
        website.setPlatform(Platform.SHOPIFY);
        website.setStatus(WebsiteStatus.ACTIVE);

        Order existing = new Order();
        existing.setId(UUID.randomUUID());
        existing.setOrganization(organization);
        existing.setWebsite(website);
        existing.setExternalOrderId(externalOrderId);
        existing.setStatus(OrderStatus.CREATED);
        existing.setFinancialStatus(FinancialStatus.UNKNOWN);
        existing.setFulfillmentStatus(FulfillmentOverallStatus.UNKNOWN);
        existing.setOrderTotal(BigDecimal.ZERO);
        existing.setIngestedAt(OffsetDateTime.now(ZoneOffset.UTC));

        OrderCreateRequest request = new OrderCreateRequest();
        request.setOrgId(orgId);
        request.setWebsiteId(websiteId);
        request.setExternalOrderId(externalOrderId);
        request.setOrderTotal(new BigDecimal("12.50"));

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(websiteRepository.findById(websiteId)).thenReturn(Optional.of(website));
        when(repository.findByOrganizationIdAndWebsiteIdAndExternalOrderId(orgId, websiteId, externalOrderId))
                .thenReturn(Optional.of(existing));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = service.create(request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(repository).save(captor.capture());
        Order saved = captor.getValue();

        assertThat(saved).isSameAs(existing);
        assertThat(response.getOrgId()).isEqualTo(orgId);
        assertThat(response.getWebsiteId()).isEqualTo(websiteId);
        assertThat(response.getExternalOrderId()).isEqualTo(externalOrderId);
    }

    @Test
    void createThrowsWhenWebsiteDoesNotBelongToOrg() {
        UUID orgId = UUID.randomUUID();
        UUID websiteId = UUID.randomUUID();

        Organization org = new Organization();
        org.setId(orgId);

        Organization other = new Organization();
        other.setId(UUID.randomUUID());

        Website website = new Website();
        website.setId(websiteId);
        website.setOrganization(other);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setOrgId(orgId);
        request.setWebsiteId(websiteId);
        request.setExternalOrderId("ext-order-2");

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(websiteRepository.findById(websiteId)).thenReturn(Optional.of(website));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Website does not belong to the specified organization");

        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void listUsesOrderUpdatedAtDefaultSort() {
        UUID orgId = UUID.randomUUID();

        Organization organization = new Organization();
        organization.setId(orgId);

        Website website = new Website();
        website.setId(UUID.randomUUID());
        website.setOrganization(organization);

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setOrganization(organization);
        order.setWebsite(website);
        order.setExternalOrderId("ext-order-3");
        order.setStatus(OrderStatus.CREATED);
        order.setFinancialStatus(FinancialStatus.UNKNOWN);
        order.setFulfillmentStatus(FulfillmentOverallStatus.UNKNOWN);
        order.setOrderTotal(BigDecimal.ZERO);
        order.setIngestedAt(OffsetDateTime.now(ZoneOffset.UTC));

        PageRequest expected = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "orderUpdatedAt"));
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order), expected, 1));

        PagedResponse<OrderResponse> response = service.list(orgId, null, null, null, null, null, null, null, null, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(50);
        assertThat(pageable.getSort().getOrderFor("orderUpdatedAt")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("orderUpdatedAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(response.getData()).hasSize(1);
    }

    @Test
    void getThrowsWhenMissing() {
        UUID orderId = UUID.randomUUID();
        when(repository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(orderId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order not found");
    }
}
