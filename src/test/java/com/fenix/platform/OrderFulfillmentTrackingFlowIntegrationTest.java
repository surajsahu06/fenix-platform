package com.fenix.platform;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import com.fenix.platform.dto.ErrorResponse;
import com.fenix.platform.dto.FulfillmentCreateRequest;
import com.fenix.platform.dto.FulfillmentResponse;
import com.fenix.platform.dto.OrderCreateRequest;
import com.fenix.platform.dto.OrderResponse;
import com.fenix.platform.dto.OrganizationCreateRequest;
import com.fenix.platform.dto.OrganizationResponse;
import com.fenix.platform.dto.TrackingCreateRequest;
import com.fenix.platform.dto.TrackingResponse;
import com.fenix.platform.dto.WebsiteCreateRequest;
import com.fenix.platform.dto.WebsiteResponse;
import com.fenix.platform.model.FulfillmentStatus;
import com.fenix.platform.model.Platform;
import com.fenix.platform.model.TrackingStatus;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class OrderFulfillmentTrackingFlowIntegrationTest {
    private static final String USERNAME = "fenix";
    private static final String PASSWORD = "fenix123";

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void configureAuth() {
        restTemplate = restTemplate.withBasicAuth(USERNAME, PASSWORD);
    }

    @Test
    void endToEndFlowCreatesAndFetchesResources() {
        OrganizationCreateRequest orgRequest = new OrganizationCreateRequest();
        orgRequest.setName(uniqueValue("Acme Corp"));
        ResponseEntity<OrganizationResponse> orgResponse =
                restTemplate.postForEntity("/organizations", orgRequest, OrganizationResponse.class);
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgId = orgResponse.getBody().getId();

        WebsiteCreateRequest websiteRequest = new WebsiteCreateRequest();
        websiteRequest.setCode("STORE-1");
        websiteRequest.setName("Acme Store");
        websiteRequest.setPlatform(Platform.SHOPIFY);
        ResponseEntity<WebsiteResponse> websiteResponse = restTemplate.postForEntity(
                "/organizations/" + orgId + "/websites", websiteRequest, WebsiteResponse.class);
        assertThat(websiteResponse.getStatusCode().value()).isEqualTo(201);
        UUID websiteId = websiteResponse.getBody().getId();

        OrderCreateRequest orderRequest = new OrderCreateRequest();
        orderRequest.setOrgId(orgId);
        orderRequest.setWebsiteId(websiteId);
        orderRequest.setExternalOrderId("EXT-123");
        orderRequest.setOrderTotal(new BigDecimal("125.50"));
        orderRequest.setCurrency("USD");
        ResponseEntity<OrderResponse> orderResponse = restTemplate.postForEntity("/orders", orderRequest, OrderResponse.class);
        assertThat(orderResponse.getStatusCode().value()).isEqualTo(201);
        UUID orderId = orderResponse.getBody().getId();

        FulfillmentCreateRequest fulfillmentRequest = new FulfillmentCreateRequest();
        fulfillmentRequest.setExternalFulfillmentId("FUL-123");
        fulfillmentRequest.setStatus(FulfillmentStatus.CREATED);
        ResponseEntity<FulfillmentResponse> fulfillmentResponse = restTemplate.postForEntity(
                "/orders/" + orderId + "/fulfillments", fulfillmentRequest, FulfillmentResponse.class);
        assertThat(fulfillmentResponse.getStatusCode().value()).isEqualTo(201);
        UUID fulfillmentId = fulfillmentResponse.getBody().getId();

        TrackingCreateRequest trackingRequest = new TrackingCreateRequest();
        trackingRequest.setTrackingNumber("1Z999");
        trackingRequest.setStatus(TrackingStatus.IN_TRANSIT);
        trackingRequest.setIsPrimary(true);
        ResponseEntity<TrackingResponse> trackingResponse = restTemplate.postForEntity(
                "/fulfillments/" + fulfillmentId + "/tracking", trackingRequest, TrackingResponse.class);
        assertThat(trackingResponse.getStatusCode().value()).isEqualTo(201);
        UUID trackingId = trackingResponse.getBody().getId();

        ResponseEntity<OrderResponse> fetchedOrder = restTemplate.getForEntity("/orders/" + orderId, OrderResponse.class);
        assertThat(fetchedOrder.getStatusCode().value()).isEqualTo(200);
        assertThat(fetchedOrder.getBody().getId()).isEqualTo(orderId);

        ResponseEntity<FulfillmentResponse> fetchedFulfillment = restTemplate.getForEntity(
                "/orders/" + orderId + "/fulfillments/" + fulfillmentId, FulfillmentResponse.class);
        assertThat(fetchedFulfillment.getStatusCode().value()).isEqualTo(200);

        ResponseEntity<TrackingResponse> fetchedTracking = restTemplate.getForEntity(
                "/fulfillments/" + fulfillmentId + "/tracking/" + trackingId, TrackingResponse.class);
        assertThat(fetchedTracking.getStatusCode().value()).isEqualTo(200);

        ResponseEntity<String> deleteTracking = restTemplate.exchange(
                "/fulfillments/" + fulfillmentId + "/tracking/" + trackingId,
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                String.class);
        assertThat(deleteTracking.getStatusCode().value()).isEqualTo(204);
    }

    @Test
    void tenantIsolationPreventsCrossOrgWebsiteUsage() {
        OrganizationCreateRequest orgRequest = new OrganizationCreateRequest();
        orgRequest.setName(uniqueValue("Org One"));
        ResponseEntity<OrganizationResponse> orgResponse =
                restTemplate.postForEntity("/organizations", orgRequest, OrganizationResponse.class);
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgOneId = orgResponse.getBody().getId();

        OrganizationCreateRequest orgTwoRequest = new OrganizationCreateRequest();
        orgTwoRequest.setName(uniqueValue("Org Two"));
        ResponseEntity<OrganizationResponse> orgTwoResponse =
                restTemplate.postForEntity("/organizations", orgTwoRequest, OrganizationResponse.class);
        assertThat(orgTwoResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgTwoId = orgTwoResponse.getBody().getId();

        WebsiteCreateRequest websiteRequest = new WebsiteCreateRequest();
        websiteRequest.setCode("ORG2-STORE");
        websiteRequest.setName("Org2 Store");
        websiteRequest.setPlatform(Platform.SHOPIFY);
        ResponseEntity<WebsiteResponse> websiteResponse = restTemplate.postForEntity(
                "/organizations/" + orgTwoId + "/websites", websiteRequest, WebsiteResponse.class);
        assertThat(websiteResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgTwoWebsiteId = websiteResponse.getBody().getId();

        OrderCreateRequest orderRequest = new OrderCreateRequest();
        orderRequest.setOrgId(orgOneId);
        orderRequest.setWebsiteId(orgTwoWebsiteId);
        orderRequest.setExternalOrderId("CROSS-ORG-ORDER");
        orderRequest.setOrderTotal(new BigDecimal("10.00"));
        ResponseEntity<ErrorResponse> orderResponse = restTemplate.postForEntity("/orders", orderRequest, ErrorResponse.class);
        assertThat(orderResponse.getStatusCode().value()).isEqualTo(404);
        assertThat(orderResponse.getBody().getMessage())
                .contains("Website not found");
    }

    @Test
    void ordersPaginationReturnsPagedResults() {
        ResponseEntity<OrganizationResponse> orgResponse = createOrganizationResponse(uniqueValue("Paged Org"));
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgId = orgResponse.getBody().getId();
        ResponseEntity<WebsiteResponse> websiteResponse = createWebsiteResponse(orgId, "PAGED-STORE", "Paged Store");
        assertThat(websiteResponse.getStatusCode().value()).isEqualTo(201);
        UUID websiteId = websiteResponse.getBody().getId();

        ResponseEntity<OrderResponse> page1 = createOrderResponse(orgId, websiteId, "PAGE-1");
        ResponseEntity<OrderResponse> page2 = createOrderResponse(orgId, websiteId, "PAGE-2");
        assertThat(page1.getStatusCode().value()).isEqualTo(201);
        assertThat(page2.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<Map> pageOne = restTemplate.getForEntity(
                "/orders?orgId=" + orgId + "&websiteId=" + websiteId + "&page=0&size=1",
                Map.class);
        assertThat(pageOne.getStatusCode().value()).isEqualTo(200);
        assertThat(pageOne.getBody().get("size")).isEqualTo(1);
        assertThat(((java.util.List<?>) pageOne.getBody().get("data")).size()).isEqualTo(1);

        ResponseEntity<Map> pageTwo = restTemplate.getForEntity(
                "/orders?orgId=" + orgId + "&websiteId=" + websiteId + "&page=1&size=1",
                Map.class);
        assertThat(pageTwo.getStatusCode().value()).isEqualTo(200);
        assertThat(pageTwo.getBody().get("size")).isEqualTo(1);
        assertThat(((java.util.List<?>) pageTwo.getBody().get("data")).size()).isEqualTo(1);
    }

    @Test
    void ordersSearchFiltersByExternalOrderId() {
        ResponseEntity<OrganizationResponse> orgResponse = createOrganizationResponse(uniqueValue("Search Org"));
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgId = orgResponse.getBody().getId();
        ResponseEntity<WebsiteResponse> websiteResponse = createWebsiteResponse(orgId, "SEARCH-STORE", "Search Store");
        assertThat(websiteResponse.getStatusCode().value()).isEqualTo(201);
        UUID websiteId = websiteResponse.getBody().getId();

        ResponseEntity<OrderResponse> search1 = createOrderResponse(orgId, websiteId, "SEARCH-1");
        ResponseEntity<OrderResponse> search2 = createOrderResponse(orgId, websiteId, "SEARCH-2");
        assertThat(search1.getStatusCode().value()).isEqualTo(201);
        assertThat(search2.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<Map> search = restTemplate.getForEntity(
                "/orders/search?orgId=" + orgId + "&websiteId=" + websiteId + "&externalOrderId=SEARCH-2",
                Map.class);
        assertThat(search.getStatusCode().value()).isEqualTo(200);
        java.util.List<?> data = (java.util.List<?>) search.getBody().get("data");
        assertThat(data.size()).isEqualTo(1);
        Map<?, ?> item = (Map<?, ?>) data.get(0);
        assertThat(item.get("externalOrderId").toString()).isEqualTo("SEARCH-2");
    }

    private ResponseEntity<OrganizationResponse> createOrganizationResponse(String name) {
        OrganizationCreateRequest request = new OrganizationCreateRequest();
        request.setName(name);
        return restTemplate.postForEntity("/organizations", request, OrganizationResponse.class);
    }

    private ResponseEntity<WebsiteResponse> createWebsiteResponse(UUID orgId, String code, String name) {
        WebsiteCreateRequest request = new WebsiteCreateRequest();
        request.setCode(code);
        request.setName(name);
        request.setPlatform(Platform.SHOPIFY);
        return restTemplate.postForEntity(
                "/organizations/" + orgId + "/websites", request, WebsiteResponse.class);
    }

    private ResponseEntity<OrderResponse> createOrderResponse(UUID orgId, UUID websiteId, String externalOrderId) {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setOrgId(orgId);
        request.setWebsiteId(websiteId);
        request.setExternalOrderId(externalOrderId);
        request.setOrderTotal(new BigDecimal("5.00"));
        return restTemplate.postForEntity("/orders", request, OrderResponse.class);
    }

    private String uniqueValue(String base) {
        return base + "-" + UUID.randomUUID();
    }
}
