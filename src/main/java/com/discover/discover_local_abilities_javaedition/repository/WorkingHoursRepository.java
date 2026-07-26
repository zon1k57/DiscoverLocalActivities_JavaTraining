package com.discover.discover_local_abilities_javaedition.repository;

import com.discover.discover_local_abilities_javaedition.model.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {
}
