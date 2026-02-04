package com.fenix.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.paging")
public class PagingProperties {
    private int defaultSize = 50;
    private int maxSize = 200;
    private String defaultSort = "updatedAt,desc";

    public int getDefaultSize() {
        return defaultSize;
    }

    public void setDefaultSize(int defaultSize) {
        this.defaultSize = defaultSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public String getDefaultSort() {
        return defaultSort;
    }

    public void setDefaultSort(String defaultSort) {
        this.defaultSort = defaultSort;
    }
}
