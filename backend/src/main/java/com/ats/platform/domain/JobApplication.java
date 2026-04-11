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
}
