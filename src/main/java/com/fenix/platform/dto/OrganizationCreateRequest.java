package com.fenix.platform.dto;

import com.fenix.platform.model.OrgStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrganizationCreateRequest {
    @NotBlank
    private String name;

    private OrgStatus status;
}
