package com.ats.platform.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ats.platform.AbstractSpringBootIntegrationTest;
import com.ats.platform.api.auth.AuthResponse;
import com.ats.platform.api.auth.RegisterRequest;
import com.ats.platform.api.job.dto.JobPostingCreateRequest;
import com.ats.platform.api.job.dto.JobPostingResponse;
import com.ats.platform.api.job.dto.MyApplicationResponse;
import com.ats.platform.domain.EmploymentType;
import com.ats.platform.domain.JobPostingStatus;
import com.ats.platform.domain.UserRole;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class Phase3ResumeIntegrationTest extends AbstractSpringBootIntegrationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	private static HttpHeaders bearer(String token) {
		HttpHeaders h = new HttpHeaders();
		h.setBearerAuth(token);
		return h;
	}

	private static byte[] pdfMentioning(String text) throws IOException {
		try (PDDocument doc = new PDDocument()) {
			PDPage page = new PDPage();
			doc.addPage(page);
			try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
				cs.beginText();
				cs.setFont(PDType1Font.HELVETICA, 12);
				cs.newLineAtOffset(50, 700);
				cs.showText(text);
				cs.endText();
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			doc.save(baos);
			return baos.toByteArray();
		}
	}

	@Test
	void uploadResume_computesMatchScore_visibleToRecruiter() throws Exception {
		var recReg = new RegisterRequest("rec-p3@test.local", "password12", UserRole.RECRUITER, "P3 Corp");
		ResponseEntity<AuthResponse> recAuth = restTemplate.postForEntity("/api/auth/register", recReg, AuthResponse.class);
		assertThat(recAuth.getBody()).isNotNull();
		String recToken = recAuth.getBody().accessToken();

		var create = new JobPostingCreateRequest(
				"Backend Engineer",
				"We need strong Java Spring Boot PostgreSQL microservices experience.",
				"Remote",
				EmploymentType.FULL_TIME,
				null,
				null,
				JobPostingStatus.OPEN);
		ResponseEntity<JobPostingResponse> created = restTemplate.postForEntity(
				"/api/recruiter/jobs",
				new HttpEntity<>(create, bearerJson(recToken)),
				JobPostingResponse.class);
		assertThat(created.getBody()).isNotNull();
		Long jobId = created.getBody().id();

		var candReg = new RegisterRequest("cand-p3@test.local", "password12", UserRole.CANDIDATE, null);
		ResponseEntity<AuthResponse> candAuth = restTemplate.postForEntity("/api/auth/register", candReg, AuthResponse.class);
		String candToken = candAuth.getBody().accessToken();

		ResponseEntity<MyApplicationResponse> applied = restTemplate.postForEntity(
				"/api/jobs/" + jobId + "/applications",
				new HttpEntity<>(bearerJson(candToken)),
				MyApplicationResponse.class);
		assertThat(applied.getBody()).isNotNull();
		long applicationId = applied.getBody().id();

		byte[] pdf = pdfMentioning("Java Spring Boot PostgreSQL microservices kubernetes docker");

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(candToken);
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new ByteArrayResource(pdf) {
			@Override
			public String getFilename() {
				return "resume.pdf";
			}
		});

		ResponseEntity<MyApplicationResponse> uploaded = restTemplate.postForEntity(
				"/api/me/applications/" + applicationId + "/resume",
				new HttpEntity<>(body, headers),
				MyApplicationResponse.class);
		assertThat(uploaded.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(uploaded.getBody()).isNotNull();
		MyApplicationResponse uploadedBody = uploaded.getBody();
		assertThat(uploadedBody.resumeUploaded()).isTrue();
		assertThat(uploadedBody.matchScore()).isNotNull();
		assertThat(uploadedBody.matchScore()).isBetween(1, 100);
		assertThat(uploadedBody.matchReasons()).isNotEmpty();

		ResponseEntity<String> appsRaw = restTemplate.exchange(
				"/api/recruiter/jobs/" + jobId + "/applications",
				HttpMethod.GET,
				new HttpEntity<>(bearerJson(recToken)),
				String.class);
		assertThat(appsRaw.getStatusCode()).isEqualTo(HttpStatus.OK);
		JsonNode root = objectMapper.readTree(appsRaw.getBody());
		JsonNode content = root.has("content") ? root.get("content") : null;
		if (content == null && root.has("page")) {
			content = root.get("content");
		}
		assertThat(content).isNotNull();
		assertThat(content.size()).isEqualTo(1);
		assertThat(content.get(0).get("matchScore").asInt()).isEqualTo(uploadedBody.matchScore());
	}

	private static HttpHeaders bearerJson(String token) {
		HttpHeaders h = bearer(token);
		h.setContentType(MediaType.APPLICATION_JSON);
		return h;
	}
}
