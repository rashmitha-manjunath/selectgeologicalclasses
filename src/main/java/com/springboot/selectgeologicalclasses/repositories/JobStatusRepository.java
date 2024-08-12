package com.springboot.selectgeologicalclasses.repositories;

import com.springboot.selectgeologicalclasses.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobStatusRepository extends JpaRepository<JobStatus, Long> {
}