package com.ats.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
		name = "applications",
		uniqueConstraints = @UniqueConstraint(columnNames = {"job_posting_id", "candidate_user_id"})
)
public class JobApplication extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "job_posting_id", nullable = false)
	private JobPosting jobPosting;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "candidate_user_id", nullable = false)
	private User candidate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ApplicationStatus status = ApplicationStatus.SUBMITTED;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@Column(name = "resume_storage_key", length = 512)
	private String resumeStorageKey;

	@Column(name = "resume_original_filename", length = 255)
	private String resumeOriginalFilename;

	@Column(name = "resume_mime_type", length = 128)
	private String resumeMimeType;

	@Column(name = "resume_uploaded_at")
	private Instant resumeUploadedAt;

	@Column(name = "resume_parsed_text", columnDefinition = "TEXT")
	private String resumeParsedText;

	@Column(name = "match_score")
	private Integer matchScore;

	/** JSON array of explanation strings; see Flyway V3. */
	@Column(name = "match_reasons", columnDefinition = "TEXT")
	private String matchReasonsJson;

	protected JobApplication() {
	}

	public JobApplication(JobPosting jobPosting, User candidate, ApplicationStatus status) {
		this.jobPosting = jobPosting;
		this.candidate = candidate;
		this.status = status;
	}

	public JobPosting getJobPosting() {
		return jobPosting;
	}

	public User getCandidate() {
		return candidate;
	}

	public ApplicationStatus getStatus() {
		return status;
	}

	public String getNotes() {
		return notes;
	}

	public void setStatus(ApplicationStatus status) {
		this.status = status;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getResumeStorageKey() {
		return resumeStorageKey;
	}

	public String getResumeOriginalFilename() {
		return resumeOriginalFilename;
	}

	public String getResumeMimeType() {
		return resumeMimeType;
	}

	public Instant getResumeUploadedAt() {
		return resumeUploadedAt;
	}

	public String getResumeParsedText() {
		return resumeParsedText;
	}

	public Integer getMatchScore() {
		return matchScore;
	}

	public String getMatchReasonsJson() {
		return matchReasonsJson;
	}

	public void clearResumeData() {
		this.resumeStorageKey = null;
		this.resumeOriginalFilename = null;
		this.resumeMimeType = null;
		this.resumeUploadedAt = null;
		this.resumeParsedText = null;
		this.matchScore = null;
		this.matchReasonsJson = null;
	}

	public void setResumeFile(
			String storageKey,
			String originalFilename,
			String mimeType,
			Instant uploadedAt,
			String parsedText,
			Integer matchScore,
			String matchReasonsJson) {
		this.resumeStorageKey = storageKey;
		this.resumeOriginalFilename = originalFilename;
		this.resumeMimeType = mimeType;
		this.resumeUploadedAt = uploadedAt;
		this.resumeParsedText = parsedText;
		this.matchScore = matchScore;
		this.matchReasonsJson = matchReasonsJson;
	}
}
