package com.fenix.platform.dto;

import com.fenix.platform.model.Platform;
import com.fenix.platform.model.WebsiteStatus;
import lombok.Data;

@Data
public class WebsitePatchRequest {
    private String code;
    private String name;
    private Platform platform;
    private WebsiteStatus status;
}
