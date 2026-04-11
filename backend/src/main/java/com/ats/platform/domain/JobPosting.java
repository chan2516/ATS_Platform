package com.ats.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_postings")
public class JobPosting extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(nullable = false, length = 500)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	private String location;

	@Enumerated(EnumType.STRING)
	@Column(name = "employment_type", length = 32)
	private EmploymentType employmentType;

	@Column(name = "salary_min")
	private Integer salaryMin;

	@Column(name = "salary_max")
	private Integer salaryMax;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private JobPostingStatus status = JobPostingStatus.OPEN;

	@OneToMany(mappedBy = "jobPosting")
	private List<JobApplication> applications = new ArrayList<>();

	protected JobPosting() {
	}

	public JobPosting(Company company, String title, JobPostingStatus status) {
		this.company = company;
		this.title = title;
		this.status = status;
	}

	public Company getCompany() {
		return company;
	}

	public String getTitle() {
		return title;
	}

	public JobPostingStatus getStatus() {
		return status;
	}
}
