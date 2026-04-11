package com.ats.platform.api.auth;

public record AuthResponse(
		String accessToken,
		String tokenType,
		long expiresInSeconds
) {
}
