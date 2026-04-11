package com.ats.platform.api.job.dto;

import com.ats.platform.domain.EmploymentType;
import com.ats.platform.domain.JobPostingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobPostingUpdateRequest(
		@NotBlank @Size(max = 500) String title,
		@Size(max = 20000) String description,
		@Size(max = 255) String location,
		EmploymentType employmentType,
		Integer salaryMin,
		Integer salaryMax,
		JobPostingStatus status
) {
}
