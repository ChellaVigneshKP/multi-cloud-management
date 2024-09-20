package com.multicloud.vms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloudAccountDto {
    private String cloudProvider;
    private String credentials;
    private String region;
    private Long userId;
}
