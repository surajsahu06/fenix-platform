package com.fenix.platform.dto;

import com.fenix.platform.model.OrgStatus;

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
public class OrganizationResponse {
    private UUID id;
    private String name;
    private OrgStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
