package com.ats.platform.api.job.dto;

import com.ats.platform.domain.ApplicationStatus;
import com.ats.platform.domain.JobApplication;
import com.ats.platform.util.MatchReasonsJson;

import java.time.Instant;
import java.util.List;

/** Candidate view of their own applications. */
public record MyApplicationResponse(
		Long id,
		Long jobPostingId,
		String jobTitle,
		String companyName,
		ApplicationStatus status,
		Instant createdAt,
		boolean resumeUploaded,
		Integer matchScore,
		List<String> matchReasons,
		Instant resumeUploadedAt
) {
	public static MyApplicationResponse fromEntity(JobApplication a) {
		return new MyApplicationResponse(
				a.getId(),
				a.getJobPosting().getId(),
				a.getJobPosting().getTitle(),
				a.getJobPosting().getCompany().getName(),
				a.getStatus(),
				a.getCreatedAt(),
				a.getResumeStorageKey() != null,
				a.getMatchScore(),
				MatchReasonsJson.readList(a.getMatchReasonsJson()),
				a.getResumeUploadedAt());
	}
}
