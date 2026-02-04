package com.fenix.platform.dto;

import com.fenix.platform.model.Platform;
import com.fenix.platform.model.WebsiteStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WebsiteCreateRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotNull
    private Platform platform;

    private WebsiteStatus status;
}
