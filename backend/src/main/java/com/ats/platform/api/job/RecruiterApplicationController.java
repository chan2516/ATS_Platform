package com.ats.platform.api.job;

import com.ats.platform.api.job.dto.ApplicationPatchRequest;
import com.ats.platform.api.job.dto.RecruiterApplicationResponse;
import com.ats.platform.job.JobApplicationService;
import com.ats.platform.security.UserPrincipal;
import com.ats.platform.util.Pageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruiter")
@Tag(name = "Recruiter applications")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterApplicationController {

	private final JobApplicationService jobApplicationService;

	public RecruiterApplicationController(JobApplicationService jobApplicationService) {
		this.jobApplicationService = jobApplicationService;
	}

	@GetMapping("/jobs/{jobId}/applications")
	@Operation(summary = "List applications for a job in my company")
	public Page<RecruiterApplicationResponse> listForJob(
			@PathVariable Long jobId,
			Pageable pageable,
			@AuthenticationPrincipal UserPrincipal principal) {
		return jobApplicationService.listForJob(principal, jobId, Pageables.normalize(pageable));
	}

	@PatchMapping("/applications/{applicationId}")
	@Operation(summary = "Update application status and recruiter notes")
	public RecruiterApplicationResponse patch(
			@PathVariable Long applicationId,
			@Valid @RequestBody ApplicationPatchRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return jobApplicationService.patchApplication(principal, applicationId, request);
	}
}
