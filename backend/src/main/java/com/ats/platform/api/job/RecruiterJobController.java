package com.ats.platform.api.job;

import com.ats.platform.api.job.dto.JobPostingCreateRequest;
import com.ats.platform.api.job.dto.JobPostingResponse;
import com.ats.platform.api.job.dto.JobPostingUpdateRequest;
import com.ats.platform.job.JobPostingService;
import com.ats.platform.security.UserPrincipal;
import com.ats.platform.util.Pageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruiter/jobs")
@Tag(name = "Recruiter jobs")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterJobController {

	private final JobPostingService jobPostingService;

	public RecruiterJobController(JobPostingService jobPostingService) {
		this.jobPostingService = jobPostingService;
	}

	@GetMapping
	@Operation(summary = "List jobs for my company")
	public Page<JobPostingResponse> list(Pageable pageable, @AuthenticationPrincipal UserPrincipal principal) {
		return jobPostingService.listCompanyJobs(principal, Pageables.normalize(pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get job by id (must belong to my company)")
	public JobPostingResponse get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
		return jobPostingService.getJobForRecruiter(principal, id);
	}

	@PostMapping
	@Operation(summary = "Create job posting")
	public JobPostingResponse create(
			@Valid @RequestBody JobPostingCreateRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return jobPostingService.createJob(principal, request);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update job posting")
	public JobPostingResponse update(
			@PathVariable Long id,
			@Valid @RequestBody JobPostingUpdateRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return jobPostingService.updateJob(principal, id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete job posting")
	public void delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
		jobPostingService.deleteJob(principal, id);
	}
}
