package com.ats.platform.api;

import com.ats.platform.AbstractSpringBootIntegrationTest;
import com.ats.platform.api.auth.AuthResponse;
import com.ats.platform.api.auth.LoginRequest;
import com.ats.platform.api.auth.RegisterRequest;
import com.ats.platform.api.user.UserResponse;
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

class AuthIntegrationTest extends AbstractSpringBootIntegrationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void registerCandidate_login_me() {
		var reg = new RegisterRequest("cand-flow@test.local", "password12", UserRole.CANDIDATE, null);
		ResponseEntity<AuthResponse> r1 = restTemplate.postForEntity("/api/auth/register", reg, AuthResponse.class);
		assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(r1.getBody()).isNotNull();
		assertThat(r1.getBody().accessToken()).isNotBlank();
		assertThat(r1.getBody().tokenType()).isEqualTo("Bearer");

		var login = new LoginRequest("cand-flow@test.local", "password12");
		ResponseEntity<AuthResponse> r2 = restTemplate.postForEntity("/api/auth/login", login, AuthResponse.class);
		assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(r2.getBody()).isNotNull();

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(r2.getBody().accessToken());
		ResponseEntity<UserResponse> me = restTemplate.exchange(
				"/api/me", HttpMethod.GET, new HttpEntity<>(headers), UserResponse.class);
		assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(me.getBody()).isNotNull();
		assertThat(me.getBody().email()).isEqualTo("cand-flow@test.local");
		assertThat(me.getBody().role()).isEqualTo(UserRole.CANDIDATE);
	}

	@Test
	void registerDuplicateEmail_returns409() {
		var first = new RegisterRequest("dup@test.local", "password12", UserRole.CANDIDATE, null);
		assertThat(restTemplate.postForEntity("/api/auth/register", first, AuthResponse.class).getStatusCode())
				.isEqualTo(HttpStatus.OK);

		var second = new RegisterRequest("dup@test.local", "password12", UserRole.CANDIDATE, null);
		ResponseEntity<ApiError> conflict = restTemplate.postForEntity("/api/auth/register", second, ApiError.class);
		assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(conflict.getBody()).isNotNull();
		assertThat(conflict.getBody().message()).contains("Email");
	}

	@Test
	void candidateCannotAccessRecruiterProbe() {
		var reg = new RegisterRequest("cand-probe@test.local", "password12", UserRole.CANDIDATE, null);
		ResponseEntity<AuthResponse> auth = restTemplate.postForEntity("/api/auth/register", reg, AuthResponse.class);
		assertThat(auth.getBody()).isNotNull();

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(auth.getBody().accessToken());
		ResponseEntity<ApiError> forbidden = restTemplate.exchange(
				"/api/phase1/recruiter-only",
				HttpMethod.GET,
				new HttpEntity<>(headers),
				ApiError.class);
		assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void recruiterWithCompany_canAccessRecruiterProbe() {
		var reg = new RegisterRequest("rec-probe@test.local", "password12", UserRole.RECRUITER, "Acme Corp");
		ResponseEntity<AuthResponse> auth = restTemplate.postForEntity("/api/auth/register", reg, AuthResponse.class);
		assertThat(auth.getBody()).isNotNull();

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(auth.getBody().accessToken());
		ResponseEntity<String> ok = restTemplate.exchange(
				"/api/phase1/recruiter-only",
				HttpMethod.GET,
				new HttpEntity<>(headers),
				String.class);
		assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void me_withoutToken_returns401() {
		ResponseEntity<ApiError> res = restTemplate.getForEntity("/api/me", ApiError.class);
		assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
}
