package com.ats.platform.repository;

import com.ats.platform.domain.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long>, JpaSpecificationExecutor<JobPosting> {

	@EntityGraph(attributePaths = "company")
	@Query("SELECT j FROM JobPosting j WHERE j.id = :id")
	Optional<JobPosting> findDetailById(@Param("id") Long id);

	@EntityGraph(attributePaths = "company")
	Page<JobPosting> findByCompany_Id(Long companyId, Pageable pageable);
}
