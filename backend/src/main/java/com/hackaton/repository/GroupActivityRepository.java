package com.hackaton.repository;

import com.hackaton.model.GroupActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupActivityRepository extends JpaRepository<GroupActivity, Long> {

    List<GroupActivity> findByGroupId(Long groupId);

    List<GroupActivity> findByActivityId(Long activityId);

    boolean existsByGroupIdAndActivityId(Long groupId, Long activityId);

    void deleteByGroupIdAndActivityId(Long groupId, Long activityId);
}
