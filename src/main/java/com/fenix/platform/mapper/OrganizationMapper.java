package com.fenix.platform.mapper;

import com.fenix.platform.dto.OrganizationCreateRequest;
import com.fenix.platform.dto.OrganizationPatchRequest;
import com.fenix.platform.dto.OrganizationResponse;
import com.fenix.platform.dto.OrganizationUpdateRequest;
import com.fenix.platform.entity.Organization;
import com.fenix.platform.model.OrgStatus;

public final class OrganizationMapper {
    private OrganizationMapper() {
    }

    public static OrganizationResponse toResponse(Organization entity) {
        return OrganizationResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static void applyCreate(Organization entity, OrganizationCreateRequest request) {
        entity.setName(request.getName());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : OrgStatus.ACTIVE);
    }

    public static void applyUpdate(Organization entity, OrganizationUpdateRequest request) {
        entity.setName(request.getName());
        entity.setStatus(request.getStatus());
    }

    public static void applyPatch(Organization entity, OrganizationPatchRequest request) {
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
    }
}
