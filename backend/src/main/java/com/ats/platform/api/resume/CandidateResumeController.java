package com.ats.platform.api.resume;

import com.ats.platform.api.job.dto.MyApplicationResponse;
import com.ats.platform.resume.CandidateResumeService;
import com.ats.platform.security.UserPrincipal;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me/applications")
public class CandidateResumeController {

	private final CandidateResumeService candidateResumeService;

	public CandidateResumeController(CandidateResumeService candidateResumeService) {
		this.candidateResumeService = candidateResumeService;
	}

	@PostMapping(value = "/{applicationId}/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public MyApplicationResponse upload(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Long applicationId,
			@RequestPart("file") MultipartFile file) {
		return candidateResumeService.uploadResume(principal, applicationId, file);
	}
}
