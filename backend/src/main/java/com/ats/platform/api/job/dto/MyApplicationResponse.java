package com.ats.platform.api.job.dto;

import com.ats.platform.domain.ApplicationStatus;
import com.ats.platform.domain.JobApplication;

import java.time.Instant;

/** Candidate view of their own applications. */
public record MyApplicationResponse(
		Long id,
		Long jobPostingId,
		String jobTitle,
		String companyName,
		ApplicationStatus status,
		Instant createdAt
) {
	public static MyApplicationResponse fromEntity(JobApplication a) {
		return new MyApplicationResponse(
				a.getId(),
				a.getJobPosting().getId(),
				a.getJobPosting().getTitle(),
				a.getJobPosting().getCompany().getName(),
				a.getStatus(),
				a.getCreatedAt());
	}
}
