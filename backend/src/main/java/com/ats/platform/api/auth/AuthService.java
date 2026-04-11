package com.ats.platform.api.auth;

import com.ats.platform.api.BadRequestException;
import com.ats.platform.api.ConflictException;
import com.ats.platform.domain.Company;
import com.ats.platform.domain.User;
import com.ats.platform.domain.UserRole;
import com.ats.platform.repository.CompanyRepository;
import com.ats.platform.repository.UserRepository;
import com.ats.platform.security.JwtService;
import com.ats.platform.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final long expirationMs;

	public AuthService(
			UserRepository userRepository,
			CompanyRepository companyRepository,
			PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			@Value("${app.jwt.expiration-ms}") long expirationMs) {
		this.userRepository = userRepository;
		this.companyRepository = companyRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.expirationMs = expirationMs;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (request.role() == UserRole.ADMIN) {
			throw new BadRequestException("Self-registration as ADMIN is not allowed");
		}
		String email = request.email().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("Email already registered");
		}
		Company company = null;
		if (request.role() == UserRole.RECRUITER
				&& request.companyName() != null
				&& !request.companyName().isBlank()) {
			company = companyRepository.save(new Company(request.companyName().trim()));
		}
		User user = new User(
				email,
				passwordEncoder.encode(request.password()),
				request.role(),
				company,
				true);
		user = userRepository.save(user);
		return buildAuthResponse(user);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase();
		final Authentication authentication;
		try {
			authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(email, request.password()));
		}
		catch (BadCredentialsException e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
		}
		catch (DisabledException e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account disabled");
		}
		UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
		User user = userRepository.findByEmail(principal.getEmail())
				.orElseThrow(() -> new BadRequestException("User not found"));
		return buildAuthResponse(user);
	}

	private AuthResponse buildAuthResponse(User user) {
		String token = jwtService.createAccessToken(user);
		return new AuthResponse(token, "Bearer", expirationMs / 1000);
	}
}
