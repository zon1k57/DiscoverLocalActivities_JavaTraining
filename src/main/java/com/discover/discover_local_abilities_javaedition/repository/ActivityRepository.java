package com.discover.discover_local_abilities_javaedition.repository;

import com.discover.discover_local_abilities_javaedition.model.Activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Query(value = """
        SELECT * FROM activity
        WHERE latitude BETWEEN :minLat AND :maxLat
          AND longitude BETWEEN :minLon AND :maxLon
          AND (:category IS NULL OR LOWER(type) LIKE CONCAT('%', LOWER(CAST(:category AS text)), '%'))
          AND (:minRating IS NULL OR rating >= :minRating)
          AND (:minRatingCount IS NULL OR user_rating_count >= :minRatingCount)
        """, nativeQuery = true)
    List<Activity> findWithinBoundingBox(
        @Param("minLat") Double minLat, @Param("maxLat") Double maxLat,
        @Param("minLon") Double minLon, @Param("maxLon") Double maxLon,
        @Param("category") String category,
        @Param("minRating") Double minRating,
        @Param("minRatingCount") Integer minRatingCount
    );
}
