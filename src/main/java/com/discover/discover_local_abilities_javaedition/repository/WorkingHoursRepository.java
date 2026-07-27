package com.discover.discover_local_abilities_javaedition.repository;

import com.discover.discover_local_abilities_javaedition.model.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {
    List<WorkingHours> findByActivityId_IdIn(List<Long> activityIds);
}
