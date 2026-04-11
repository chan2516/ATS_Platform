package com.ats.platform.security;

import com.ats.platform.domain.User;
import com.ats.platform.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserRepository userRepository;

	public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		String context = request.getContextPath();
		if (context != null && !context.isEmpty() && path.startsWith(context)) {
			path = path.substring(context.length());
		}
		return path.startsWith("/api/auth/")
				|| path.startsWith("/actuator/")
				|| path.startsWith("/v3/api-docs")
				|| path.startsWith("/swagger-ui");
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7).trim();
			if (!token.isEmpty()) {
				if (!jwtService.isValid(token)) {
					writeUnauthorized(response, "Invalid or expired token");
					return;
				}
				String email = jwtService.extractEmail(token);
				User user = userRepository.findByEmail(email).orElse(null);
				if (user == null || !user.isEnabled()) {
					writeUnauthorized(response, "User not found or disabled");
					return;
				}
				UserPrincipal principal = UserPrincipal.from(user);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						principal, null, principal.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
		filterChain.doFilter(request, response);
	}

	private static void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		String body = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + escapeJson(message) + "\"}";
		response.getWriter().write(body);
	}

	private static String escapeJson(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
