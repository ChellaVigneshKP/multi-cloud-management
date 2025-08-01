package com.multicloud.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUserDto {
    @Email(message = "Please provide a valid email address.")
    @NotNull(message = "Email is required.")
    private String email;
    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String password;
    @NotBlank(message = "Visitor ID is required.")
    private String visitorId;
    private boolean remember;
}