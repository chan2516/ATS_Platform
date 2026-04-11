package com.ats.platform.api.job.dto;

import com.ats.platform.domain.ApplicationStatus;
import com.ats.platform.domain.JobApplication;
import com.ats.platform.util.MatchReasonsJson;

import java.time.Instant;
import java.util.List;

/** Recruiter view: includes candidate email and internal notes. */
public record RecruiterApplicationResponse(
		Long id,
		Long jobPostingId,
		String jobTitle,
		Long candidateId,
		String candidateEmail,
		ApplicationStatus status,
		String notes,
		Instant createdAt,
		boolean resumeUploaded,
		String resumeFileName,
		Integer matchScore,
		List<String> matchReasons,
		Instant resumeUploadedAt
) {
	public static RecruiterApplicationResponse fromEntity(JobApplication a) {
		return new RecruiterApplicationResponse(
				a.getId(),
				a.getJobPosting().getId(),
				a.getJobPosting().getTitle(),
				a.getCandidate().getId(),
				a.getCandidate().getEmail(),
				a.getStatus(),
				a.getNotes(),
				a.getCreatedAt(),
				a.getResumeStorageKey() != null,
				a.getResumeOriginalFilename(),
				a.getMatchScore(),
				MatchReasonsJson.readList(a.getMatchReasonsJson()),
				a.getResumeUploadedAt());
	}
}
