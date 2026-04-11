package com.ats.platform.api.job.dto;

import com.ats.platform.domain.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationPatchRequest(
		@NotNull ApplicationStatus status,
		String notes
) {
}
