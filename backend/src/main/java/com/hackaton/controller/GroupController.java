package com.hackaton.controller;

import com.hackaton.dto.group.*;
import com.hackaton.model.enums.UserRole;
import com.hackaton.security.CustomUserDetails;
import com.hackaton.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @Valid @RequestBody CreateGroupRequest request) {
        GroupResponse response = groupService.createGroup(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<GroupResponse> response = groupService.getMyGroups(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable Long id,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupResponse response = groupService.getGroup(id, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResponse> updateGroup(@PathVariable Long id,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @Valid @RequestBody UpdateGroupRequest request) {
        UserRole userRole = UserRole.valueOf(extractRole(userDetails));
        GroupResponse response = groupService.updateGroup(id, userDetails.getId(), userRole, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserRole userRole = UserRole.valueOf(extractRole(userDetails));
        groupService.deleteGroup(id, userDetails.getId(), userRole);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<GroupMemberResponse>> getMembers(@PathVariable Long id,
                                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<GroupMemberResponse> response = groupService.getMembers(id, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    private String extractRole(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
    }
}
