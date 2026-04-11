package com.ats.platform.job;

import com.ats.platform.api.BadRequestException;
import com.ats.platform.api.NotFoundException;
import com.ats.platform.api.job.dto.JobPostingCreateRequest;
import com.ats.platform.api.job.dto.JobPostingResponse;
import com.ats.platform.api.job.dto.JobPostingUpdateRequest;
import com.ats.platform.domain.Company;
import com.ats.platform.domain.JobPosting;
import com.ats.platform.domain.JobPostingStatus;
import com.ats.platform.repository.CompanyRepository;
import com.ats.platform.repository.JobPostingRepository;
import com.ats.platform.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class JobPostingService {

	private final JobPostingRepository jobPostingRepository;
	private final CompanyRepository companyRepository;

	public JobPostingService(
			JobPostingRepository jobPostingRepository,
			CompanyRepository companyRepository) {
		this.jobPostingRepository = jobPostingRepository;
		this.companyRepository = companyRepository;
	}

	@Transactional(readOnly = true)
	public Page<JobPostingResponse> searchOpenJobs(String q, String location, Pageable pageable) {
		Specification<JobPosting> spec = Specification.allOf(
				JobSpecifications.statusOpen(),
				JobSpecifications.titleOrDescriptionContains(q),
				JobSpecifications.locationContains(location));
		return jobPostingRepository.findAll(spec, pageable).map(JobPostingResponse::fromEntity);
	}

	@Transactional(readOnly = true)
	public JobPostingResponse getPublicJob(Long id) {
		JobPosting job = jobPostingRepository.findDetailById(id)
				.orElseThrow(() -> new NotFoundException("Job not found"));
		if (job.getStatus() != JobPostingStatus.OPEN) {
			throw new NotFoundException("Job not found");
		}
		return JobPostingResponse.fromEntity(job);
	}

	@Transactional(readOnly = true)
	public Page<JobPostingResponse> listCompanyJobs(UserPrincipal principal, Pageable pageable) {
		Long companyId = requireCompany(principal);
		return jobPostingRepository.findByCompany_Id(companyId, pageable).map(JobPostingResponse::fromEntity);
	}

	@Transactional(readOnly = true)
	public JobPostingResponse getJobForRecruiter(UserPrincipal principal, Long jobId) {
		Long companyId = requireCompany(principal);
		JobPosting job = jobPostingRepository.findDetailById(jobId)
				.orElseThrow(() -> new NotFoundException("Job not found"));
		assertCompanyOwnsJob(companyId, job);
		return JobPostingResponse.fromEntity(job);
	}

	@Transactional
	public JobPostingResponse createJob(UserPrincipal principal, JobPostingCreateRequest req) {
		Long companyId = requireCompany(principal);
		Company company = companyRepository.findById(companyId)
				.orElseThrow(() -> new NotFoundException("Company not found"));
		validateSalary(req.salaryMin(), req.salaryMax());
		JobPostingStatus status = req.status() != null ? req.status() : JobPostingStatus.OPEN;
		JobPosting job = new JobPosting(company, req.title(), status);
		applyCommonFields(job, req.description(), req.location(), req.employmentType(), req.salaryMin(), req.salaryMax());
		job = jobPostingRepository.save(job);
		return JobPostingResponse.fromEntity(jobPostingRepository.findDetailById(job.getId()).orElseThrow());
	}

	@Transactional
	public JobPostingResponse updateJob(UserPrincipal principal, Long jobId, JobPostingUpdateRequest req) {
		Long companyId = requireCompany(principal);
		validateSalary(req.salaryMin(), req.salaryMax());
		JobPosting job = jobPostingRepository.findDetailById(jobId)
				.orElseThrow(() -> new NotFoundException("Job not found"));
		assertCompanyOwnsJob(companyId, job);
		job.setTitle(req.title());
		job.setStatus(req.status());
		applyCommonFields(job, req.description(), req.location(), req.employmentType(), req.salaryMin(), req.salaryMax());
		return JobPostingResponse.fromEntity(job);
	}

	@Transactional
	public void deleteJob(UserPrincipal principal, Long jobId) {
		Long companyId = requireCompany(principal);
		JobPosting job = jobPostingRepository.findById(jobId)
				.orElseThrow(() -> new NotFoundException("Job not found"));
		assertCompanyOwnsJob(companyId, job);
		jobPostingRepository.delete(job);
	}

	private static void applyCommonFields(
			JobPosting job,
			String description,
			String location,
			com.ats.platform.domain.EmploymentType employmentType,
			Integer salaryMin,
			Integer salaryMax) {
		job.setDescription(description);
		job.setLocation(location);
		job.setEmploymentType(employmentType);
		job.setSalaryMin(salaryMin);
		job.setSalaryMax(salaryMax);
	}

	private static void validateSalary(Integer min, Integer max) {
		if (min != null && max != null && min > max) {
			throw new BadRequestException("salaryMin cannot be greater than salaryMax");
		}
	}

	private static void assertCompanyOwnsJob(Long companyId, JobPosting job) {
		if (!Objects.equals(job.getCompany().getId(), companyId)) {
			throw new NotFoundException("Job not found");
		}
	}

	private Long requireCompany(UserPrincipal principal) {
		if (principal.getCompanyId() == null) {
			throw new BadRequestException("Recruiter must belong to a company to manage jobs");
		}
		return principal.getCompanyId();
	}
}
