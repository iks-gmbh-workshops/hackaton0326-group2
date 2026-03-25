package com.hackaton.repository;

import com.hackaton.model.GroupMember;
import com.hackaton.model.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByUserIdAndStatus(Long userId, MemberStatus status);

    List<GroupMember> findByGroupId(Long groupId);

    Optional<GroupMember> findByUserIdAndGroupId(Long userId, Long groupId);

    boolean existsByUserIdAndGroupId(Long userId, Long groupId);
}
