package com.ats.platform.api.job;

import com.ats.platform.api.job.dto.MyApplicationResponse;
import com.ats.platform.job.JobApplicationService;
import com.ats.platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs/{jobId}/applications")
@Tag(name = "Applications (candidate)")
@SecurityRequirement(name = "bearer-jwt")
public class CandidateJobApplicationController {

	private final JobApplicationService jobApplicationService;

	public CandidateJobApplicationController(JobApplicationService jobApplicationService) {
		this.jobApplicationService = jobApplicationService;
	}

	@PostMapping
	@PreAuthorize("hasRole('CANDIDATE')")
	@Operation(summary = "Apply to an open job (one per candidate per job)")
	public MyApplicationResponse apply(
			@PathVariable Long jobId,
			@AuthenticationPrincipal UserPrincipal principal) {
		return jobApplicationService.applyAsCandidate(principal.getId(), jobId);
	}
}
