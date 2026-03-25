package com.hackaton.controller;

import com.hackaton.dto.activity.ActivityResponse;
import com.hackaton.dto.activity.AddGroupsRequest;
import com.hackaton.dto.activity.CreateActivityRequest;
import com.hackaton.dto.activity.UpdateActivityRequest;
import com.hackaton.model.enums.UserRole;
import com.hackaton.security.CustomUserDetails;
import com.hackaton.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                            @Valid @RequestBody CreateActivityRequest request) {
        ActivityResponse response = activityService.createActivity(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable Long id,
                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        ActivityResponse response = activityService.getActivity(id, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse> updateActivity(@PathVariable Long id,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails,
                                                            @Valid @RequestBody UpdateActivityRequest request) {
        UserRole userRole = UserRole.valueOf(extractRole(userDetails));
        ActivityResponse response = activityService.updateActivity(id, userDetails.getId(), userRole, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserRole userRole = UserRole.valueOf(extractRole(userDetails));
        activityService.deleteActivity(id, userDetails.getId(), userRole);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/groups")
    public ResponseEntity<ActivityResponse> addGroupsToActivity(@PathVariable Long id,
                                                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                                                @Valid @RequestBody AddGroupsRequest request) {
        UserRole userRole = UserRole.valueOf(extractRole(userDetails));
        ActivityResponse response = activityService.addGroupsToActivity(id, userDetails.getId(), userRole, request.getGroupIds());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/groups/{gid}")
    public ResponseEntity<Void> removeGroupFromActivity(@PathVariable Long id,
                                                         @PathVariable Long gid,
                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserRole userRole = UserRole.valueOf(extractRole(userDetails));
        activityService.removeGroupFromActivity(id, gid, userDetails.getId(), userRole);
        return ResponseEntity.noContent().build();
    }

    private String extractRole(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
    }
}
