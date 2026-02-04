package com.fenix.platform.outbox;

public final class OutboxAggregateType {
    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String WEBSITE = "WEBSITE";
    public static final String ORDER = "ORDER";
    public static final String FULFILLMENT = "FULFILLMENT";
    public static final String TRACKING = "TRACKING";

    private OutboxAggregateType() {
    }
}
