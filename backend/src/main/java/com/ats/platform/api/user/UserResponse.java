package com.ats.platform.api.user;

import com.ats.platform.domain.UserRole;

public record UserResponse(
		Long id,
		String email,
		UserRole role,
		Long companyId
) {
}
