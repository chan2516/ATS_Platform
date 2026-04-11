package com.ats.platform.api.user;

import com.ats.platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@Tag(name = "Current user")
public class UserController {

	@GetMapping("/me")
	@Operation(summary = "Current authenticated user")
	@SecurityRequirement(name = "bearer-jwt")
	public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
		if (principal == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
		}
		return new UserResponse(principal.getId(), principal.getEmail(), principal.getRole(), principal.getCompanyId());
	}
}
