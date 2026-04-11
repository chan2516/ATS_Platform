package com.ats.platform.api.job.dto;

import com.ats.platform.domain.EmploymentType;
import com.ats.platform.domain.JobPosting;
import com.ats.platform.domain.JobPostingStatus;

import java.time.Instant;

public record JobPostingResponse(
		Long id,
		Long companyId,
		String companyName,
		String title,
		String description,
		String location,
		EmploymentType employmentType,
		Integer salaryMin,
		Integer salaryMax,
		JobPostingStatus status,
		Instant createdAt
) {
	public static JobPostingResponse fromEntity(JobPosting j) {
		return new JobPostingResponse(
				j.getId(),
				j.getCompany().getId(),
				j.getCompany().getName(),
				j.getTitle(),
				j.getDescription(),
				j.getLocation(),
				j.getEmploymentType(),
				j.getSalaryMin(),
				j.getSalaryMax(),
				j.getStatus(),
				j.getCreatedAt());
	}
}
