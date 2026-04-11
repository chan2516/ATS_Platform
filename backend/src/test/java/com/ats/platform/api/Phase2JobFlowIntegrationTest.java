package com.ats.platform.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ats.platform.AbstractSpringBootIntegrationTest;
import com.ats.platform.api.job.dto.ApplicationPatchRequest;
import com.ats.platform.api.job.dto.JobPostingCreateRequest;
import com.ats.platform.api.job.dto.JobPostingResponse;
import com.ats.platform.api.job.dto.MyApplicationResponse;
import com.ats.platform.api.job.dto.RecruiterApplicationResponse;
import com.ats.platform.domain.ApplicationStatus;
import com.ats.platform.domain.JobPostingStatus;
import com.ats.platform.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class Phase2JobFlowIntegrationTest extends AbstractSpringBootIntegrationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	private static HttpHeaders json(String token) {
		HttpHeaders h = new HttpHeaders();
		h.setBearerAuth(token);
		h.add("Content-Type", "application/json");
		return h;
	}

	@Test
	void recruiterCreatesJob_candidateApplies_recruiterUpdatesStatus() throws Exception {
		var recReg = new com.ats.platform.api.auth.RegisterRequest(
				"rec-phase2@test.local", "password12", UserRole.RECRUITER, "Phase2 Corp");
		ResponseEntity<com.ats.platform.api.auth.AuthResponse> recAuth = restTemplate.postForEntity(
				"/api/auth/register", recReg, com.ats.platform.api.auth.AuthResponse.class);
		assertThat(recAuth.getBody()).isNotNull();
		String recToken = recAuth.getBody().accessToken();

		var create = new JobPostingCreateRequest(
				"Senior Engineer",
				"Build things",
				"Remote",
				com.ats.platform.domain.EmploymentType.FULL_TIME,
				100000,
				150000,
				JobPostingStatus.OPEN);
		ResponseEntity<JobPostingResponse> created = restTemplate.postForEntity(
				"/api/recruiter/jobs",
				new HttpEntity<>(create, json(recToken)),
				JobPostingResponse.class);
		assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(created.getBody()).isNotNull();
		Long jobId = created.getBody().id();

		ResponseEntity<JobPostingResponse> publicJob = restTemplate.getForEntity(
				"/api/jobs/" + jobId, JobPostingResponse.class);
		assertThat(publicJob.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(publicJob.getBody().title()).isEqualTo("Senior Engineer");

		var candReg = new com.ats.platform.api.auth.RegisterRequest(
				"cand-phase2@test.local", "password12", UserRole.CANDIDATE, null);
		ResponseEntity<com.ats.platform.api.auth.AuthResponse> candAuth = restTemplate.postForEntity(
				"/api/auth/register", candReg, com.ats.platform.api.auth.AuthResponse.class);
		String candToken = candAuth.getBody().accessToken();

		ResponseEntity<MyApplicationResponse> applied = restTemplate.postForEntity(
				"/api/jobs/" + jobId + "/applications",
				new HttpEntity<>(json(candToken)),
				MyApplicationResponse.class);
		assertThat(applied.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(applied.getBody().jobTitle()).isEqualTo("Senior Engineer");

		ResponseEntity<String> appsRaw = restTemplate.exchange(
				"/api/recruiter/jobs/" + jobId + "/applications",
				HttpMethod.GET,
				new HttpEntity<>(json(recToken)),
				String.class);
		assertThat(appsRaw.getStatusCode()).isEqualTo(HttpStatus.OK);
		JsonNode content = objectMapper.readTree(appsRaw.getBody()).get("content");
		assertThat(content.size()).isEqualTo(1);
		long applicationId = content.get(0).get("id").asLong();

		var patch = new ApplicationPatchRequest(ApplicationStatus.SCREENING, "Phone screen next week");
		ResponseEntity<RecruiterApplicationResponse> patched = restTemplate.exchange(
				"/api/recruiter/applications/" + applicationId,
				HttpMethod.PATCH,
				new HttpEntity<>(patch, json(recToken)),
				RecruiterApplicationResponse.class);
		assertThat(patched.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(patched.getBody().status()).isEqualTo(ApplicationStatus.SCREENING);
		assertThat(patched.getBody().notes()).isEqualTo("Phone screen next week");
	}
}
