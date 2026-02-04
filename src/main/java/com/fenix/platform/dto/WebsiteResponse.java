package com.fenix.platform.dto;

import com.fenix.platform.model.Platform;
import com.fenix.platform.model.WebsiteStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteResponse {
    private UUID id;
    private UUID orgId;
    private String code;
    private String name;
    private Platform platform;
    private WebsiteStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
