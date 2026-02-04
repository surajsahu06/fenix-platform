package com.fenix.platform;

import static org.assertj.core.api.Assertions.assertThat;

import com.fenix.platform.dto.ErrorResponse;
import com.fenix.platform.dto.FulfillmentCreateRequest;
import com.fenix.platform.dto.FulfillmentResponse;
import com.fenix.platform.dto.OrderCreateRequest;
import com.fenix.platform.dto.OrderPatchRequest;
import com.fenix.platform.dto.OrderResponse;
import com.fenix.platform.dto.OrganizationCreateRequest;
import com.fenix.platform.dto.OrganizationResponse;
import com.fenix.platform.dto.PagedResponse;
import com.fenix.platform.dto.TrackingCreateRequest;
import com.fenix.platform.dto.TrackingResponse;
import com.fenix.platform.dto.WebsiteCreateRequest;
import com.fenix.platform.dto.WebsiteResponse;
import com.fenix.platform.entity.Fulfillment;
import com.fenix.platform.entity.Order;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.entity.Tracking;
import com.fenix.platform.entity.Website;
import com.fenix.platform.model.FulfillmentStatus;
import com.fenix.platform.model.OrgStatus;
import com.fenix.platform.model.OrderStatus;
import com.fenix.platform.model.Platform;
import com.fenix.platform.model.TrackingStatus;
import com.fenix.platform.model.WebsiteStatus;
import com.fenix.platform.repository.FulfillmentRepository;
import com.fenix.platform.repository.OrderRepository;
import com.fenix.platform.repository.OrganizationRepository;
import com.fenix.platform.repository.TrackingRepository;
import com.fenix.platform.repository.WebsiteRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ApiRequestResponseIntegrationTest {
    private static final String USERNAME = "fenix";
    private static final String PASSWORD = "fenix123";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private WebsiteRepository websiteRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FulfillmentRepository fulfillmentRepository;

    @Autowired
    private TrackingRepository trackingRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void configureAuth() {
        restTemplate = restTemplate.withBasicAuth(USERNAME, PASSWORD);
    }

    @Test
    void createOrganizationPersistsAndReturnsFields() {
        OrganizationCreateRequest request = new OrganizationCreateRequest();
        String orgName = uniqueValue("Acme Inc");
        request.setName(orgName);
        request.setStatus(OrgStatus.INACTIVE);

        ResponseEntity<OrganizationResponse> response =
                restTemplate.postForEntity("/organizations", request, OrganizationResponse.class);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        OrganizationResponse body = response.getBody();

        UUID orgId = body.getId();
        assertThat(body.getName()).isEqualTo(orgName);
        assertThat(body.getStatus()).isEqualTo(OrgStatus.INACTIVE);
        assertThat(body.getCreatedAt()).isNotNull();
        assertThat(body.getUpdatedAt()).isNotNull();

        Organization organization = organizationRepository.findById(orgId).orElseThrow();
        assertThat(organization.getName()).isEqualTo(orgName);
        assertThat(organization.getStatus()).isEqualTo(OrgStatus.INACTIVE);
        assertThat(organization.getCreatedAt()).isNotNull();
        assertThat(organization.getUpdatedAt()).isNotNull();
    }

    @Test
    void unauthorizedRequestReturns401() {
        TestRestTemplate unauthenticated = new TestRestTemplate();
        ResponseEntity<String> response =
                unauthenticated.getForEntity(baseUrl() + "/organizations", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void invalidCredentialsReturn401() {
        TestRestTemplate invalidAuth = new TestRestTemplate().withBasicAuth("bad-user", "bad-pass");
        ResponseEntity<String> response =
                invalidAuth.getForEntity(baseUrl() + "/organizations", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void missingAuthOnPostReturns401() {
        TestRestTemplate unauthenticated = new TestRestTemplate();
        OrganizationCreateRequest request = new OrganizationCreateRequest();
        request.setName("Unauthorized Org");

        ResponseEntity<String> response = unauthenticated.postForEntity(
                baseUrl() + "/organizations", request, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void missingAuthOnPatchReturns401() {
        TestRestTemplate unauthenticated = new TestRestTemplate();
        OrderPatchRequest request = new OrderPatchRequest();
        request.setExternalOrderNumber("NO-AUTH-PATCH");

        HttpEntity<OrderPatchRequest> entity = new HttpEntity<>(request);
        ResponseEntity<String> response = unauthenticated.exchange(
                baseUrl() + "/orders/" + UUID.randomUUID(), HttpMethod.PATCH, entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void missingAuthOnDeleteReturns401() {
        TestRestTemplate unauthenticated = new TestRestTemplate();
        ResponseEntity<String> response = unauthenticated.exchange(
                baseUrl() + "/organizations/" + UUID.randomUUID(), HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void createWebsiteDefaultsStatusAndPersists() {
        ResponseEntity<OrganizationResponse> orgResponse = createOrganizationResponse(uniqueValue("Website Org"));
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgId = orgResponse.getBody().getId();
        WebsiteCreateRequest request = new WebsiteCreateRequest();
        String storeCode = uniqueValue("STORE-1");
        String storeName = uniqueValue("Main Store");
        request.setCode(storeCode);
        request.setName(storeName);
        request.setPlatform(Platform.SHOPIFY);

        ResponseEntity<WebsiteResponse> response = restTemplate.postForEntity(
                "/organizations/" + orgId + "/websites", request, WebsiteResponse.class);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        WebsiteResponse body = response.getBody();

        UUID websiteId = body.getId();
        assertThat(body.getOrgId()).isEqualTo(orgId);
        assertThat(body.getCode()).isEqualTo(storeCode);
        assertThat(body.getPlatform()).isEqualTo(Platform.SHOPIFY);
        assertThat(body.getStatus()).isEqualTo(WebsiteStatus.ACTIVE);

        Website website = websiteRepository.findById(websiteId).orElseThrow();
        assertThat(website.getOrganization().getId()).isEqualTo(orgId);
        assertThat(website.getCode()).isEqualTo(storeCode);
        assertThat(website.getPlatform()).isEqualTo(Platform.SHOPIFY);
        assertThat(website.getStatus()).isEqualTo(WebsiteStatus.ACTIVE);
    }

    @Test
    void createOrderReturnsAndPersistsRequestFields() {
        ResponseEntity<OrganizationResponse> orgResponse = createOrganizationResponse(uniqueValue("Order Org"));
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgId = orgResponse.getBody().getId();
        ResponseEntity<WebsiteResponse> websiteResponse =
                createWebsiteResponse(orgId, uniqueValue("ORDER-STORE"), uniqueValue("Order Store"));
        assertThat(websiteResponse.getStatusCode().value()).isEqualTo(201);
        UUID websiteId = websiteResponse.getBody().getId();

        String externalOrderId = uniqueValue("EXT-100");
        String externalOrderNumber = uniqueValue("ORD-100");
        OrderCreateRequest request = new OrderCreateRequest();
        request.setOrgId(orgId);
        request.setWebsiteId(websiteId);
        request.setExternalOrderId(externalOrderId);
        request.setExternalOrderNumber(externalOrderNumber);
        request.setStatus(OrderStatus.CANCELLED);
        request.setFinancialStatus(com.fenix.platform.model.FinancialStatus.PAID);
        request.setFulfillmentStatus(com.fenix.platform.model.FulfillmentOverallStatus.PARTIAL);
        request.setCustomerEmail("buyer@example.com");
        request.setOrderTotal(new BigDecimal("123.45"));
        request.setCurrency("USD");
        request.setOrderCreatedAt(OffsetDateTime.parse("2026-02-03T10:15:30Z"));
        request.setOrderUpdatedAt(OffsetDateTime.parse("2026-02-03T11:15:30Z"));

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity("/orders", request, OrderResponse.class);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        OrderResponse body = response.getBody();
        UUID orderId = body.getId();

        assertThat(body.getOrgId()).isEqualTo(orgId);
        assertThat(body.getWebsiteId()).isEqualTo(websiteId);
        assertThat(body.getExternalOrderId()).isEqualTo(externalOrderId);
        assertThat(body.getExternalOrderNumber()).isEqualTo(externalOrderNumber);
        assertThat(body.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(body.getFinancialStatus()).isEqualTo(com.fenix.platform.model.FinancialStatus.PAID);
        assertThat(body.getFulfillmentStatus()).isEqualTo(com.fenix.platform.model.FulfillmentOverallStatus.PARTIAL);
        assertThat(body.getCustomerEmail()).isEqualTo("buyer@example.com");
        assertThat(body.getOrderTotal().compareTo(new BigDecimal("123.45"))).isZero();
        assertThat(body.getCurrency()).isEqualTo("USD");
        assertThat(body.getIngestedAt()).isNotNull();

        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getOrganization().getId()).isEqualTo(orgId);
        assertThat(order.getWebsite().getId()).isEqualTo(websiteId);
        assertThat(order.getExternalOrderId()).isEqualTo(externalOrderId);
        assertThat(order.getExternalOrderNumber()).isEqualTo(externalOrderNumber);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getOrderTotal().compareTo(new BigDecimal("123.45"))).isZero();
        assertThat(order.getCurrency()).isEqualTo("USD");
        assertThat(order.getOrderCreatedAt()).isEqualTo(OffsetDateTime.parse("2026-02-03T10:15:30Z"));
        assertThat(order.getOrderUpdatedAt()).isEqualTo(OffsetDateTime.parse("2026-02-03T11:15:30Z"));
    }

    @Test
    void patchOrderUpdatesFieldsAndDb() {
        ResponseEntity<OrganizationResponse> orgResponse = createOrganizationResponse(uniqueValue("Patch Org"));
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgId = orgResponse.getBody().getId();
        ResponseEntity<WebsiteResponse> websiteResponse =
                createWebsiteResponse(orgId, uniqueValue("PATCH-STORE"), uniqueValue("Patch Store"));
        assertThat(websiteResponse.getStatusCode().value()).isEqualTo(201);
        UUID websiteId = websiteResponse.getBody().getId();
        ResponseEntity<OrderResponse> orderResponse = createOrderResponse(orgId, websiteId, uniqueValue("PATCH-1"));
        assertThat(orderResponse.getStatusCode().value()).isEqualTo(201);
        UUID orderId = orderResponse.getBody().getId();

        Map<String, Object> patchRequest = new HashMap<>();
        patchRequest.put("status", "CLOSED");
        patchRequest.put("orderTotal", 200.00);
        patchRequest.put("currency", "EUR");

        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                "/orders/" + orderId,
                HttpMethod.PATCH,
                new HttpEntity<>(patchRequest),
                OrderResponse.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        OrderResponse body = response.getBody();
        assertThat(body.getStatus()).isEqualTo(OrderStatus.CLOSED);
        assertThat(body.getOrderTotal().compareTo(new BigDecimal("200.00"))).isZero();
        assertThat(body.getCurrency()).isEqualTo("EUR");

        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CLOSED);
        assertThat(order.getOrderTotal().compareTo(new BigDecimal("200.00"))).isZero();
        assertThat(order.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void createFulfillmentAndTrackingPersistsAndReturnsFields() {
        ResponseEntity<OrganizationResponse> orgResponse = createOrganizationResponse(uniqueValue("Fulfillment Org"));
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgId = orgResponse.getBody().getId();
        ResponseEntity<WebsiteResponse> websiteResponse = createWebsiteResponse(orgId, "FUL-STORE", "Fulfillment Store");
        assertThat(websiteResponse.getStatusCode().value()).isEqualTo(201);
        UUID websiteId = websiteResponse.getBody().getId();
        ResponseEntity<OrderResponse> orderResponse = createOrderResponse(orgId, websiteId, "FUL-ORDER");
        assertThat(orderResponse.getStatusCode().value()).isEqualTo(201);
        UUID orderId = orderResponse.getBody().getId();

        FulfillmentCreateRequest fulfillmentRequest = new FulfillmentCreateRequest();
        fulfillmentRequest.setExternalFulfillmentId("FUL-1");
        fulfillmentRequest.setStatus(FulfillmentStatus.SHIPPED);
        fulfillmentRequest.setCarrier("UPS");
        fulfillmentRequest.setServiceLevel("GROUND");
        fulfillmentRequest.setShippedAt(OffsetDateTime.parse("2026-02-03T12:00:00Z"));

        ResponseEntity<FulfillmentResponse> fulfillmentResponse = restTemplate.postForEntity(
                "/orders/" + orderId + "/fulfillments", fulfillmentRequest, FulfillmentResponse.class);
        assertThat(fulfillmentResponse.getStatusCode().value()).isEqualTo(201);
        FulfillmentResponse fulfillmentBody = fulfillmentResponse.getBody();
        UUID fulfillmentId = fulfillmentBody.getId();
        assertThat(fulfillmentBody.getExternalFulfillmentId()).isEqualTo("FUL-1");
        assertThat(fulfillmentBody.getStatus()).isEqualTo(FulfillmentStatus.SHIPPED);
        assertThat(fulfillmentBody.getCarrier()).isEqualTo("UPS");

        Fulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId).orElseThrow();
        assertThat(fulfillment.getOrder().getId()).isEqualTo(orderId);
        assertThat(fulfillment.getStatus()).isEqualTo(FulfillmentStatus.SHIPPED);
        assertThat(fulfillment.getCarrier()).isEqualTo("UPS");

        TrackingCreateRequest trackingRequest = new TrackingCreateRequest();
        trackingRequest.setTrackingNumber("1ZTRACK");
        trackingRequest.setStatus(TrackingStatus.IN_TRANSIT);
        trackingRequest.setIsPrimary(true);
        trackingRequest.setCarrier("UPS");

        ResponseEntity<TrackingResponse> trackingResponse = restTemplate.postForEntity(
                "/fulfillments/" + fulfillmentId + "/tracking", trackingRequest, TrackingResponse.class);
        assertThat(trackingResponse.getStatusCode().value()).isEqualTo(201);
        TrackingResponse trackingBody = trackingResponse.getBody();
        UUID trackingId = trackingBody.getId();
        assertThat(trackingBody.getTrackingNumber()).isEqualTo("1ZTRACK");
        assertThat(trackingBody.getStatus()).isEqualTo(TrackingStatus.IN_TRANSIT);
        assertThat(trackingBody.isPrimary()).isTrue();

        Tracking tracking = trackingRepository.findById(trackingId).orElseThrow();
        assertThat(tracking.getFulfillment().getId()).isEqualTo(fulfillmentId);
        assertThat(tracking.getStatus()).isEqualTo(TrackingStatus.IN_TRANSIT);
        assertThat(tracking.isPrimary()).isTrue();
    }

    @Test
    void validationErrorReturnsStructuredResponse() {
        OrganizationCreateRequest request = new OrganizationCreateRequest();
        request.setName("");

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/organizations", request, ErrorResponse.class);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        ErrorResponse body = response.getBody();
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getError()).isEqualTo("Bad Request");
        assertThat(body.getMessage()).contains("must not be blank");
        assertThat(body.getPath()).isEqualTo("/organizations");
    }

    @Test
    void organizationsListSupportsPaginationAndSorting() {
        String orgPrefix = uniqueValue("Org-List");
        String alphaName = orgPrefix + "-Alpha";
        String betaName = orgPrefix + "-Beta";
        String gammaName = orgPrefix + "-Gamma";
        ResponseEntity<OrganizationResponse> beta = createOrganizationResponse(betaName);
        ResponseEntity<OrganizationResponse> alpha = createOrganizationResponse(alphaName);
        ResponseEntity<OrganizationResponse> gamma = createOrganizationResponse(gammaName);
        assertThat(beta.getStatusCode().value()).isEqualTo(201);
        assertThat(alpha.getStatusCode().value()).isEqualTo(201);
        assertThat(gamma.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<PagedResponse<OrganizationResponse>> pageOne = restTemplate.exchange(
                "/organizations?name=" + orgPrefix + "&page=0&size=2&sort=name,asc",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                });
        assertThat(pageOne.getStatusCode().value()).isEqualTo(200);
        PagedResponse<OrganizationResponse> body = pageOne.getBody();
        assertThat(body.getPage()).isEqualTo(0);
        assertThat(body.getSize()).isEqualTo(2);
        assertThat(body.getTotalElements()).isEqualTo(3);
        assertThat(body.getTotalPages()).isEqualTo(2);
        assertThat(body.isHasNext()).isTrue();
        assertThat(body.getData().size()).isEqualTo(2);
        assertThat(body.getData().get(0).getName()).isEqualTo(alphaName);
        assertThat(body.getData().get(1).getName()).isEqualTo(betaName);

        ResponseEntity<PagedResponse<OrganizationResponse>> pageTwo = restTemplate.exchange(
                "/organizations?name=" + orgPrefix + "&page=1&size=2&sort=name,asc",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                });
        assertThat(pageTwo.getStatusCode().value()).isEqualTo(200);
        assertThat(pageTwo.getBody().getData().size()).isEqualTo(1);
        assertThat(pageTwo.getBody().isHasNext()).isFalse();
        assertThat(pageTwo.getBody().getData().get(0).getName()).isEqualTo(gammaName);
    }

    @Test
    void websitesListSupportsPaginationAndSorting() {
        ResponseEntity<OrganizationResponse> orgResponse = createOrganizationResponse(uniqueValue("Web Org"));
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgId = orgResponse.getBody().getId();
        ResponseEntity<WebsiteResponse> beta = createWebsiteResponse(orgId, "BETA", "Beta Store");
        ResponseEntity<WebsiteResponse> alpha = createWebsiteResponse(orgId, "ALPHA", "Alpha Store");
        assertThat(beta.getStatusCode().value()).isEqualTo(201);
        assertThat(alpha.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<PagedResponse<WebsiteResponse>> response = restTemplate.exchange(
                "/organizations/" + orgId + "/websites?page=0&size=2&sort=code,asc",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        PagedResponse<WebsiteResponse> body = response.getBody();
        assertThat(body.getData().size()).isEqualTo(2);
        assertThat(body.getData().get(0).getCode()).isEqualTo("ALPHA");
        assertThat(body.getData().get(1).getCode()).isEqualTo("BETA");
    }

    @Test
    void ordersListSupportsPaginationAndSorting() {
        ResponseEntity<OrganizationResponse> orgResponse = createOrganizationResponse(uniqueValue("Orders Org"));
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgId = orgResponse.getBody().getId();
        ResponseEntity<WebsiteResponse> websiteResponse = createWebsiteResponse(orgId, "ORDER-SORT", "Order Sort Store");
        assertThat(websiteResponse.getStatusCode().value()).isEqualTo(201);
        UUID websiteId = websiteResponse.getBody().getId();

        ResponseEntity<OrderResponse> ord2 = createOrderWithUpdatedAtResponse(orgId, websiteId, "ORD-2", "2026-02-03T10:00:00Z");
        ResponseEntity<OrderResponse> ord1 = createOrderWithUpdatedAtResponse(orgId, websiteId, "ORD-1", "2026-02-03T09:00:00Z");
        ResponseEntity<OrderResponse> ord3 = createOrderWithUpdatedAtResponse(orgId, websiteId, "ORD-3", "2026-02-03T11:00:00Z");
        assertThat(ord2.getStatusCode().value()).isEqualTo(201);
        assertThat(ord1.getStatusCode().value()).isEqualTo(201);
        assertThat(ord3.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<PagedResponse<OrderResponse>> pageOne = restTemplate.exchange(
                "/orders?orgId=" + orgId + "&websiteId=" + websiteId + "&page=0&size=2&sort=externalOrderId,asc",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                });
        assertThat(pageOne.getStatusCode().value()).isEqualTo(200);
        PagedResponse<OrderResponse> body = pageOne.getBody();
        assertThat(body.getTotalElements()).isEqualTo(3);
        assertThat(body.getTotalPages()).isEqualTo(2);
        assertThat(body.isHasNext()).isTrue();
        assertThat(body.getData().get(0).getExternalOrderId()).isEqualTo("ORD-1");
        assertThat(body.getData().get(1).getExternalOrderId()).isEqualTo("ORD-2");

        ResponseEntity<PagedResponse<OrderResponse>> pageTwo = restTemplate.exchange(
                "/orders?orgId=" + orgId + "&websiteId=" + websiteId + "&page=1&size=2&sort=externalOrderId,asc",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                });
        assertThat(pageTwo.getStatusCode().value()).isEqualTo(200);
        assertThat(pageTwo.getBody().isHasNext()).isFalse();
        assertThat(pageTwo.getBody().getData().size()).isEqualTo(1);
        assertThat(pageTwo.getBody().getData().get(0).getExternalOrderId()).isEqualTo("ORD-3");
    }

    @Test
    void ordersSearchSupportsPagination() {
        ResponseEntity<OrganizationResponse> orgResponse = createOrganizationResponse(uniqueValue("Search Org"));
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgId = orgResponse.getBody().getId();
        ResponseEntity<WebsiteResponse> websiteResponse = createWebsiteResponse(orgId, "SEARCH-SITE", "Search Store");
        assertThat(websiteResponse.getStatusCode().value()).isEqualTo(201);
        UUID websiteId = websiteResponse.getBody().getId();
        ResponseEntity<OrderResponse> search1 = createOrderResponse(orgId, websiteId, "SEARCH-1");
        ResponseEntity<OrderResponse> search2 = createOrderResponse(orgId, websiteId, "SEARCH-2");
        assertThat(search1.getStatusCode().value()).isEqualTo(201);
        assertThat(search2.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<PagedResponse<OrderResponse>> response = restTemplate.exchange(
                "/orders/search?orgId=" + orgId + "&websiteId=" + websiteId + "&externalOrderId=SEARCH-2&page=0&size=5",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        PagedResponse<OrderResponse> body = response.getBody();
        assertThat(body.getTotalElements()).isEqualTo(1);
        assertThat(body.getData().size()).isEqualTo(1);
        assertThat(body.getData().get(0).getExternalOrderId()).isEqualTo("SEARCH-2");
    }

    @Test
    void ordersListSupportsDateRangeFiltering() {
        ResponseEntity<OrganizationResponse> orgResponse = createOrganizationResponse(uniqueValue("Date Range Org"));
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgId = orgResponse.getBody().getId();
        ResponseEntity<WebsiteResponse> websiteResponse = createWebsiteResponse(orgId, "DATE-SITE", "Date Store");
        assertThat(websiteResponse.getStatusCode().value()).isEqualTo(201);
        UUID websiteId = websiteResponse.getBody().getId();

        ResponseEntity<OrderResponse> date1 = createOrderWithUpdatedAtResponse(orgId, websiteId, "DATE-1", "2026-02-03T08:00:00Z");
        ResponseEntity<OrderResponse> date2 = createOrderWithUpdatedAtResponse(orgId, websiteId, "DATE-2", "2026-02-03T09:00:00Z");
        ResponseEntity<OrderResponse> date3 = createOrderWithUpdatedAtResponse(orgId, websiteId, "DATE-3", "2026-02-03T10:00:00Z");
        assertThat(date1.getStatusCode().value()).isEqualTo(201);
        assertThat(date2.getStatusCode().value()).isEqualTo(201);
        assertThat(date3.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<PagedResponse<OrderResponse>> response = restTemplate.exchange(
                "/orders?orgId=" + orgId
                        + "&websiteId=" + websiteId
                        + "&from=2026-02-03T08:30:00Z"
                        + "&to=2026-02-03T09:30:00Z"
                        + "&page=0&size=10&sort=orderUpdatedAt,asc",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        PagedResponse<OrderResponse> body = response.getBody();
        assertThat(body.getTotalElements()).isEqualTo(1);
        assertThat(body.getData().size()).isEqualTo(1);
        assertThat(body.getData().get(0).getExternalOrderId()).isEqualTo("DATE-2");
    }

    @Test
    void fulfillmentsListSupportsPaginationAndSorting() {
        ResponseEntity<OrganizationResponse> orgResponse = createOrganizationResponse(uniqueValue("Fulfillment List Org"));
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgId = orgResponse.getBody().getId();
        ResponseEntity<WebsiteResponse> websiteResponse = createWebsiteResponse(orgId, "FUL-LIST", "Fulfillment List Store");
        assertThat(websiteResponse.getStatusCode().value()).isEqualTo(201);
        UUID websiteId = websiteResponse.getBody().getId();
        ResponseEntity<OrderResponse> orderResponse = createOrderResponse(orgId, websiteId, "FUL-ORDER");
        assertThat(orderResponse.getStatusCode().value()).isEqualTo(201);
        UUID orderId = orderResponse.getBody().getId();

        ResponseEntity<FulfillmentResponse> ful2 = createFulfillmentResponse(orderId, "FUL-2");
        ResponseEntity<FulfillmentResponse> ful1 = createFulfillmentResponse(orderId, "FUL-1");
        assertThat(ful2.getStatusCode().value()).isEqualTo(201);
        assertThat(ful1.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<PagedResponse<FulfillmentResponse>> response = restTemplate.exchange(
                "/orders/" + orderId + "/fulfillments?page=0&size=10&sort=externalFulfillmentId,asc",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getData().size()).isEqualTo(2);
        assertThat(response.getBody().getData().get(0).getExternalFulfillmentId()).isEqualTo("FUL-1");
        assertThat(response.getBody().getData().get(1).getExternalFulfillmentId()).isEqualTo("FUL-2");
    }

    @Test
    void trackingListSupportsPaginationAndSorting() {
        ResponseEntity<OrganizationResponse> orgResponse = createOrganizationResponse(uniqueValue("Tracking List Org"));
        assertThat(orgResponse.getStatusCode().value()).isEqualTo(201);
        UUID orgId = orgResponse.getBody().getId();
        ResponseEntity<WebsiteResponse> websiteResponse = createWebsiteResponse(orgId, "TRACK-LIST", "Tracking List Store");
        assertThat(websiteResponse.getStatusCode().value()).isEqualTo(201);
        UUID websiteId = websiteResponse.getBody().getId();
        ResponseEntity<OrderResponse> orderResponse = createOrderResponse(orgId, websiteId, "TRACK-ORDER");
        assertThat(orderResponse.getStatusCode().value()).isEqualTo(201);
        UUID orderId = orderResponse.getBody().getId();
        ResponseEntity<FulfillmentResponse> fulfillmentResponse = createFulfillmentResponse(orderId, "FUL-TRACK");
        assertThat(fulfillmentResponse.getStatusCode().value()).isEqualTo(201);
        UUID fulfillmentId = fulfillmentResponse.getBody().getId();

        ResponseEntity<TrackingResponse> tn2 = createTrackingResponse(fulfillmentId, "TN-2");
        ResponseEntity<TrackingResponse> tn1 = createTrackingResponse(fulfillmentId, "TN-1");
        assertThat(tn2.getStatusCode().value()).isEqualTo(201);
        assertThat(tn1.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<PagedResponse<TrackingResponse>> response = restTemplate.exchange(
                "/fulfillments/" + fulfillmentId + "/tracking?page=0&size=10&sort=trackingNumber,asc",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getData().size()).isEqualTo(2);
        assertThat(response.getBody().getData().get(0).getTrackingNumber()).isEqualTo("TN-1");
        assertThat(response.getBody().getData().get(1).getTrackingNumber()).isEqualTo("TN-2");
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

    private ResponseEntity<OrderResponse> createOrderWithUpdatedAtResponse(UUID orgId, UUID websiteId,
                                                                           String externalOrderId, String updatedAt) {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setOrgId(orgId);
        request.setWebsiteId(websiteId);
        request.setExternalOrderId(externalOrderId);
        request.setOrderTotal(new BigDecimal("5.00"));
        request.setOrderUpdatedAt(OffsetDateTime.parse(updatedAt));
        return restTemplate.postForEntity("/orders", request, OrderResponse.class);
    }

    private ResponseEntity<FulfillmentResponse> createFulfillmentResponse(UUID orderId, String externalFulfillmentId) {
        FulfillmentCreateRequest request = new FulfillmentCreateRequest();
        request.setExternalFulfillmentId(externalFulfillmentId);
        request.setStatus(FulfillmentStatus.CREATED);
        return restTemplate.postForEntity(
                "/orders/" + orderId + "/fulfillments", request, FulfillmentResponse.class);
    }

    private ResponseEntity<TrackingResponse> createTrackingResponse(UUID fulfillmentId, String trackingNumber) {
        TrackingCreateRequest request = new TrackingCreateRequest();
        request.setTrackingNumber(trackingNumber);
        request.setStatus(TrackingStatus.IN_TRANSIT);
        request.setIsPrimary(Boolean.FALSE);
        return restTemplate.postForEntity(
                "/fulfillments/" + fulfillmentId + "/tracking", request, TrackingResponse.class);
    }

    private String uniqueValue(String base) {
        return base + "-" + UUID.randomUUID();
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
