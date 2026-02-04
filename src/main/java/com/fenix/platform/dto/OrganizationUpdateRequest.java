package com.fenix.platform.dto;

import com.fenix.platform.model.OrgStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrganizationUpdateRequest {
    @NotBlank
    private String name;

    @NotNull
    private OrgStatus status;
}
