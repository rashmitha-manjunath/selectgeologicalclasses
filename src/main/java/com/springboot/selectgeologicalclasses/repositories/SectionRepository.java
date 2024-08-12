package com.springboot.selectgeologicalclasses.repositories;
import com.springboot.selectgeologicalclasses.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {
    @Query("SELECT s FROM Section s JOIN s.geologicalClasses g WHERE g.code = :code")
    List<Section> findByGeologicalClassCode(@Param("code") String code);

    Optional<Section> findByName(String name);
}
