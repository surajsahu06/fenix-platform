package com.fenix.platform.tenant;

import com.fenix.platform.entity.Fulfillment;
import com.fenix.platform.entity.Order;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.entity.Tracking;
import com.fenix.platform.entity.Website;
import com.fenix.platform.exception.NotFoundException;
import com.fenix.platform.repository.FulfillmentRepository;
import com.fenix.platform.repository.OrderRepository;
import com.fenix.platform.repository.OrganizationRepository;
import com.fenix.platform.repository.TrackingRepository;
import com.fenix.platform.repository.WebsiteRepository;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantAccessGuard {
    private final OrganizationRepository organizationRepository;
    private final WebsiteRepository websiteRepository;
    private final OrderRepository orderRepository;
    private final FulfillmentRepository fulfillmentRepository;
    private final TrackingRepository trackingRepository;

    public Organization requireOrganization(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }

    public Website requireWebsite(UUID orgId, UUID websiteId) {
        return websiteRepository.findByIdAndOrganizationId(websiteId, orgId)
                .orElseThrow(() -> new NotFoundException("Website not found"));
    }

    public Order requireOrder(UUID orgId, UUID orderId) {
        return orderRepository.findByIdAndOrganizationId(orderId, orgId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    public Fulfillment requireFulfillment(UUID orgId, UUID fulfillmentId) {
        return fulfillmentRepository.findByIdAndOrganizationId(fulfillmentId, orgId)
                .orElseThrow(() -> new NotFoundException("Fulfillment not found"));
    }

    public Tracking requireTracking(UUID orgId, UUID trackingId) {
        return trackingRepository.findByIdAndOrganizationId(trackingId, orgId)
                .orElseThrow(() -> new NotFoundException("Tracking not found"));
    }

    public void ensureWebsiteInOrganization(UUID orgId, UUID websiteId) {
        if (!websiteRepository.existsByIdAndOrganizationId(websiteId, orgId)) {
            throw new NotFoundException("Website not found");
        }
    }
}
