package com.ats.platform.security;

import com.ats.platform.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

	private final SecretKey key;
	private final long expirationMs;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-ms}") long expirationMs) {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("app.jwt.secret must be at least 256 bits (32 bytes) for HS256");
		}
		this.key = Keys.hmacShaKeyFor(bytes);
		this.expirationMs = expirationMs;
	}

	public String createAccessToken(User user) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
				.subject(user.getEmail())
				.claim("uid", user.getId())
				.claim("role", user.getRole().name())
				.issuedAt(now)
				.expiration(exp)
				.signWith(key)
				.compact();
	}

	public String extractEmail(String token) {
		return parseClaims(token).getSubject();
	}

	public Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public boolean isValid(String token) {
		try {
			parseClaims(token);
			return true;
		}
		catch (ExpiredJwtException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
			return false;
		}
	}
}
