package com.ats.platform.api.auth;

import com.ats.platform.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@Email @NotBlank @Size(max = 255) String email,
		@NotBlank @Size(min = 8, max = 128) String password,
		@NotNull UserRole role,
		@Size(max = 255) String companyName
) {
}
