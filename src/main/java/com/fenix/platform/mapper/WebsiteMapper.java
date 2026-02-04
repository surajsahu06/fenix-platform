package com.fenix.platform.mapper;

import com.fenix.platform.dto.WebsiteCreateRequest;
import com.fenix.platform.dto.WebsitePatchRequest;
import com.fenix.platform.dto.WebsiteResponse;
import com.fenix.platform.dto.WebsiteUpdateRequest;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.entity.Website;
import com.fenix.platform.model.WebsiteStatus;

public final class WebsiteMapper {
    private WebsiteMapper() {
    }

    public static WebsiteResponse toResponse(Website entity) {
        return WebsiteResponse.builder()
                .id(entity.getId())
                .orgId(entity.getOrganization().getId())
                .code(entity.getCode())
                .name(entity.getName())
                .platform(entity.getPlatform())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static void applyCreate(Website entity, Organization organization, WebsiteCreateRequest request) {
        entity.setOrganization(organization);
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setPlatform(request.getPlatform());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : WebsiteStatus.ACTIVE);
    }

    public static void applyUpdate(Website entity, WebsiteUpdateRequest request) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setPlatform(request.getPlatform());
        entity.setStatus(request.getStatus());
    }

    public static void applyPatch(Website entity, WebsitePatchRequest request) {
        if (request.getCode() != null) {
            entity.setCode(request.getCode());
        }
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getPlatform() != null) {
            entity.setPlatform(request.getPlatform());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
    }
}
