package com.ats.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

	@Column(nullable = false)
	private String name;

	@OneToMany(mappedBy = "company")
	private List<User> users = new ArrayList<>();

	@OneToMany(mappedBy = "company")
	private List<JobPosting> jobPostings = new ArrayList<>();

	protected Company() {
	}

	public Company(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
