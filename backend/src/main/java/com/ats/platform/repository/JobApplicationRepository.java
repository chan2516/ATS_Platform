package com.ats.platform.repository;

import com.ats.platform.domain.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

	boolean existsByJobPosting_IdAndCandidate_Id(Long jobPostingId, Long candidateId);

	@EntityGraph(attributePaths = "candidate")
	Page<JobApplication> findByJobPosting_Id(Long jobPostingId, Pageable pageable);

	@EntityGraph(attributePaths = {"jobPosting", "jobPosting.company"})
	Page<JobApplication> findByCandidate_Id(Long candidateId, Pageable pageable);

	@EntityGraph(attributePaths = {"jobPosting", "jobPosting.company", "candidate"})
	@Query("SELECT a FROM JobApplication a WHERE a.id = :id")
	Optional<JobApplication> findDetailById(@Param("id") Long id);
}
