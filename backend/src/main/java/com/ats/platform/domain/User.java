package com.ats.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private UserRole role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private Company company;

	@Column(nullable = false)
	private boolean enabled = true;

	@OneToMany(mappedBy = "candidate")
	private List<JobApplication> applications = new ArrayList<>();

	protected User() {
	}

	public User(String email, String passwordHash, UserRole role, Company company, boolean enabled) {
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
		this.company = company;
		this.enabled = enabled;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public UserRole getRole() {
		return role;
	}

	public Company getCompany() {
		return company;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
