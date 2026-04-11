package com.ats.platform.api.job;

import com.ats.platform.api.job.dto.JobPostingResponse;
import com.ats.platform.job.JobPostingService;
import com.ats.platform.util.Pageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs (public)")
public class PublicJobController {

	private final JobPostingService jobPostingService;

	public PublicJobController(JobPostingService jobPostingService) {
		this.jobPostingService = jobPostingService;
	}

	@GetMapping
	@Operation(summary = "Search open job postings (pagination)")
	public Page<JobPostingResponse> listOpen(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String location,
			Pageable pageable) {
		return jobPostingService.searchOpenJobs(q, location, Pageables.normalize(pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get one open job by id")
	public JobPostingResponse getOpen(@PathVariable Long id) {
		return jobPostingService.getPublicJob(id);
	}
}
