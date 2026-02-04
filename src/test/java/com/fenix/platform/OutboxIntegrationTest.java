package com.fenix.platform;

import static org.assertj.core.api.Assertions.assertThat;

import com.fenix.platform.dto.OrderCreateRequest;
import com.fenix.platform.dto.OrderResponse;
import com.fenix.platform.dto.OrganizationCreateRequest;
import com.fenix.platform.dto.OrganizationResponse;
import com.fenix.platform.dto.WebsiteCreateRequest;
import com.fenix.platform.dto.WebsiteResponse;
import com.fenix.platform.entity.OutboxEvent;
import com.fenix.platform.model.Platform;
import com.fenix.platform.outbox.OutboxAggregateType;
import com.fenix.platform.repository.OutboxEventRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class OutboxIntegrationTest {
    private static final String USERNAME = "fenix";
    private static final String PASSWORD = "fenix123";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void configureAuth() {
        restTemplate = restTemplate.withBasicAuth(USERNAME, PASSWORD);
    }

    @Test
    void createsOutboxRowsForOrganizationWebsiteAndOrder() {
        OrganizationCreateRequest orgRequest = new OrganizationCreateRequest();
        orgRequest.setName(uniqueValue("Outbox Org"));
        ResponseEntity<OrganizationResponse> orgResponse =
                restTemplate.postForEntity("/organizations", orgRequest, OrganizationResponse.class);
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgId = orgResponse.getBody().getId();

        WebsiteCreateRequest websiteRequest = new WebsiteCreateRequest();
        websiteRequest.setCode(uniqueValue("OUTBOX-STORE"));
        websiteRequest.setName(uniqueValue("Outbox Store"));
        websiteRequest.setPlatform(Platform.SHOPIFY);
        ResponseEntity<WebsiteResponse> websiteResponse = restTemplate.postForEntity(
                "/organizations/" + orgId + "/websites", websiteRequest, WebsiteResponse.class);
        assertThat(websiteResponse.getStatusCode().value()).isEqualTo(201);
        UUID websiteId = websiteResponse.getBody().getId();

        OrderCreateRequest orderRequest = new OrderCreateRequest();
        orderRequest.setOrgId(orgId);
        orderRequest.setWebsiteId(websiteId);
        orderRequest.setExternalOrderId(uniqueValue("OUTBOX-ORDER"));
        orderRequest.setOrderTotal(new BigDecimal("12.50"));
        orderRequest.setCurrency("USD");
        ResponseEntity<OrderResponse> orderResponse =
                restTemplate.postForEntity("/orders", orderRequest, OrderResponse.class);
        assertThat(orderResponse.getStatusCode().value()).isEqualTo(201);
        UUID orderId = orderResponse.getBody().getId();

        List<OutboxEvent> events = outboxEventRepository.findAll();
        assertThat(containsAggregate(events, OutboxAggregateType.ORGANIZATION, orgId)).isTrue();
        assertThat(containsAggregate(events, OutboxAggregateType.WEBSITE, websiteId)).isTrue();
        assertThat(containsAggregate(events, OutboxAggregateType.ORDER, orderId)).isTrue();
    }

    private boolean containsAggregate(List<OutboxEvent> events, String aggregateType, UUID aggregateId) {
        return events.stream().anyMatch(event ->
                aggregateType.equals(event.getAggregateType()) && aggregateId.equals(event.getAggregateId()));
    }

    private String uniqueValue(String base) {
        return base + "-" + UUID.randomUUID();
    }
}
