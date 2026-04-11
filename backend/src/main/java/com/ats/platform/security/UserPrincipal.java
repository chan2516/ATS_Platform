package com.ats.platform.security;

import com.ats.platform.domain.User;
import com.ats.platform.domain.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

	private final Long id;
	private final String email;
	private final String passwordHash;
	private final UserRole role;
	private final Long companyId;
	private final boolean enabled;

	public UserPrincipal(Long id, String email, String passwordHash, UserRole role, Long companyId, boolean enabled) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
		this.companyId = companyId;
		this.enabled = enabled;
	}

	public static UserPrincipal from(User user) {
		Long companyId = user.getCompany() != null ? user.getCompany().getId() : null;
		return new UserPrincipal(
				user.getId(),
				user.getEmail(),
				user.getPasswordHash(),
				user.getRole(),
				companyId,
				user.isEnabled());
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public UserRole getRole() {
		return role;
	}

	public Long getCompanyId() {
		return companyId;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
}
