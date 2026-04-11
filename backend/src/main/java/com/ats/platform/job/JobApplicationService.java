package com.ats.platform.job;

import com.ats.platform.api.BadRequestException;
import com.ats.platform.api.ConflictException;
import com.ats.platform.api.NotFoundException;
import com.ats.platform.api.job.dto.ApplicationPatchRequest;
import com.ats.platform.api.job.dto.MyApplicationResponse;
import com.ats.platform.api.job.dto.RecruiterApplicationResponse;
import com.ats.platform.domain.ApplicationStatus;
import com.ats.platform.domain.JobApplication;
import com.ats.platform.domain.JobPosting;
import com.ats.platform.domain.JobPostingStatus;
import com.ats.platform.domain.User;
import com.ats.platform.domain.UserRole;
import com.ats.platform.repository.JobApplicationRepository;
import com.ats.platform.repository.JobPostingRepository;
import com.ats.platform.repository.UserRepository;
import com.ats.platform.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class JobApplicationService {

	private final JobApplicationRepository jobApplicationRepository;
	private final JobPostingRepository jobPostingRepository;
	private final UserRepository userRepository;

	public JobApplicationService(
			JobApplicationRepository jobApplicationRepository,
			JobPostingRepository jobPostingRepository,
			UserRepository userRepository) {
		this.jobApplicationRepository = jobApplicationRepository;
		this.jobPostingRepository = jobPostingRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public MyApplicationResponse applyAsCandidate(Long candidateUserId, Long jobId) {
		User candidate = userRepository.findById(candidateUserId)
				.orElseThrow(() -> new NotFoundException("User not found"));
		if (candidate.getRole() != UserRole.CANDIDATE) {
			throw new BadRequestException("Only candidates can apply to jobs");
		}
		JobPosting job = jobPostingRepository.findDetailById(jobId)
				.orElseThrow(() -> new NotFoundException("Job not found"));
		if (job.getStatus() != JobPostingStatus.OPEN) {
			throw new BadRequestException("This job is not accepting applications");
		}
		if (jobApplicationRepository.existsByJobPosting_IdAndCandidate_Id(jobId, candidateUserId)) {
			throw new ConflictException("You have already applied to this job");
		}
		JobApplication app = new JobApplication(job, candidate, ApplicationStatus.SUBMITTED);
		app = jobApplicationRepository.save(app);
		return MyApplicationResponse.fromEntity(app);
	}

	@Transactional(readOnly = true)
	public Page<MyApplicationResponse> listMyApplications(Long candidateUserId, Pageable pageable) {
		return jobApplicationRepository.findByCandidate_Id(candidateUserId, pageable)
				.map(MyApplicationResponse::fromEntity);
	}

	@Transactional(readOnly = true)
	public Page<RecruiterApplicationResponse> listForJob(UserPrincipal principal, Long jobId, Pageable pageable) {
		Long companyId = requireCompany(principal);
		JobPosting job = jobPostingRepository.findDetailById(jobId)
				.orElseThrow(() -> new NotFoundException("Job not found"));
		if (!Objects.equals(job.getCompany().getId(), companyId)) {
			throw new NotFoundException("Job not found");
		}
		return jobApplicationRepository.findByJobPosting_Id(jobId, pageable)
				.map(RecruiterApplicationResponse::fromEntity);
	}

	@Transactional
	public RecruiterApplicationResponse patchApplication(UserPrincipal principal, Long applicationId, ApplicationPatchRequest req) {
		Long companyId = requireCompany(principal);
		JobApplication app = jobApplicationRepository.findDetailById(applicationId)
				.orElseThrow(() -> new NotFoundException("Application not found"));
		if (!Objects.equals(app.getJobPosting().getCompany().getId(), companyId)) {
			throw new NotFoundException("Application not found");
		}
		app.setStatus(req.status());
		app.setNotes(req.notes());
		return RecruiterApplicationResponse.fromEntity(app);
	}

	private static Long requireCompany(UserPrincipal principal) {
		if (principal.getCompanyId() == null) {
			throw new BadRequestException("Recruiter must belong to a company to manage applications");
		}
		return principal.getCompanyId();
	}
}
