package com.fenix.platform.dto;

import com.fenix.platform.model.OrgStatus;
import lombok.Data;

@Data
public class OrganizationPatchRequest {
    private String name;
    private OrgStatus status;
}
