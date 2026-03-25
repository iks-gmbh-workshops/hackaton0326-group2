package com.hackaton.repository;

import com.hackaton.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Query("SELECT DISTINCT a FROM Activity a " +
            "JOIN a.groupActivities ga " +
            "JOIN ga.group g " +
            "JOIN g.members gm " +
            "WHERE gm.user.id = :userId " +
            "AND gm.status = com.hackaton.model.enums.MemberStatus.ACTIVE " +
            "AND a.startTime > :now " +
            "ORDER BY a.startTime ASC")
    List<Activity> findUpcomingByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
