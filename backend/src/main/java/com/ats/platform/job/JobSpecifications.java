package com.ats.platform.job;

import com.ats.platform.domain.JobPosting;
import com.ats.platform.domain.JobPostingStatus;
import org.springframework.data.jpa.domain.Specification;

public final class JobSpecifications {

	private JobSpecifications() {
	}

	public static Specification<JobPosting> statusOpen() {
		return (root, q, cb) -> cb.equal(root.get("status"), JobPostingStatus.OPEN);
	}

	public static Specification<JobPosting> titleOrDescriptionContains(String q) {
		if (q == null || q.isBlank()) {
			return (root, query, cb) -> cb.conjunction();
		}
		String pattern = "%" + q.trim().toLowerCase() + "%";
		return (root, query, cb) -> cb.or(
				cb.like(cb.lower(root.get("title")), pattern),
				cb.and(
						cb.isNotNull(root.get("description")),
						cb.like(cb.lower(root.get("description")), pattern)));
	}

	public static Specification<JobPosting> locationContains(String location) {
		if (location == null || location.isBlank()) {
			return (root, query, cb) -> cb.conjunction();
		}
		String pattern = "%" + location.trim().toLowerCase() + "%";
		return (root, query, cb) -> cb.like(cb.lower(root.get("location")), pattern);
	}
}
