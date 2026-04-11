package com.ats.platform.api.job;

import com.ats.platform.api.job.dto.MyApplicationResponse;
import com.ats.platform.job.JobApplicationService;
import com.ats.platform.security.UserPrincipal;
import com.ats.platform.util.Pageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@Tag(name = "My applications")
@SecurityRequirement(name = "bearer-jwt")
public class MeApplicationsController {

	private final JobApplicationService jobApplicationService;

	public MeApplicationsController(JobApplicationService jobApplicationService) {
		this.jobApplicationService = jobApplicationService;
	}

	@GetMapping("/applications")
	@PreAuthorize("hasRole('CANDIDATE')")
	@Operation(summary = "List my applications")
	public Page<MyApplicationResponse> myApplications(
			Pageable pageable,
			@AuthenticationPrincipal UserPrincipal principal) {
		return jobApplicationService.listMyApplications(principal.getId(), Pageables.normalize(pageable));
	}
}
