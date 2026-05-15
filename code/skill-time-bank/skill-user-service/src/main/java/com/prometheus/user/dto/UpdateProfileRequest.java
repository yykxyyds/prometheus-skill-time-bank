package com.prometheus.user.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String email;
    private String phone;
    private String bio;
    private String avatar;
}
