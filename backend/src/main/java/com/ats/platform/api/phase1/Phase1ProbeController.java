package com.ats.platform.api.phase1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Minimal secured endpoints to verify role-based access (Phase 1 exit criteria).
 */
@RestController
@RequestMapping("/api/phase1")
@Tag(name = "Phase 1 — role probes")
public class Phase1ProbeController {

	@GetMapping("/recruiter-only")
	@PreAuthorize("hasRole('RECRUITER')")
	@Operation(summary = "RECRUITER role only")
	@SecurityRequirement(name = "bearer-jwt")
	public Map<String, String> recruiterOnly() {
		return Map.of("message", "ok", "requiredRole", "RECRUITER");
	}

	@GetMapping("/candidate-only")
	@PreAuthorize("hasRole('CANDIDATE')")
	@Operation(summary = "CANDIDATE role only")
	@SecurityRequirement(name = "bearer-jwt")
	public Map<String, String> candidateOnly() {
		return Map.of("message", "ok", "requiredRole", "CANDIDATE");
	}
}
