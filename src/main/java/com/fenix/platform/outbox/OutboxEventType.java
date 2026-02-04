package com.fenix.platform.outbox;

public final class OutboxEventType {
    public static final String ORGANIZATION_CREATED = "ORGANIZATION_CREATED";
    public static final String ORGANIZATION_UPDATED = "ORGANIZATION_UPDATED";
    public static final String ORGANIZATION_PATCHED = "ORGANIZATION_PATCHED";
    public static final String ORGANIZATION_DELETED = "ORGANIZATION_DELETED";

    public static final String WEBSITE_CREATED = "WEBSITE_CREATED";
    public static final String WEBSITE_UPDATED = "WEBSITE_UPDATED";
    public static final String WEBSITE_PATCHED = "WEBSITE_PATCHED";
    public static final String WEBSITE_DELETED = "WEBSITE_DELETED";

    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String ORDER_UPDATED = "ORDER_UPDATED";
    public static final String ORDER_PATCHED = "ORDER_PATCHED";
    public static final String ORDER_DELETED = "ORDER_DELETED";

    public static final String FULFILLMENT_CREATED = "FULFILLMENT_CREATED";
    public static final String FULFILLMENT_UPDATED = "FULFILLMENT_UPDATED";
    public static final String FULFILLMENT_PATCHED = "FULFILLMENT_PATCHED";
    public static final String FULFILLMENT_DELETED = "FULFILLMENT_DELETED";

    public static final String TRACKING_CREATED = "TRACKING_CREATED";
    public static final String TRACKING_UPDATED = "TRACKING_UPDATED";
    public static final String TRACKING_PATCHED = "TRACKING_PATCHED";
    public static final String TRACKING_DELETED = "TRACKING_DELETED";

    private OutboxEventType() {
    }
}
