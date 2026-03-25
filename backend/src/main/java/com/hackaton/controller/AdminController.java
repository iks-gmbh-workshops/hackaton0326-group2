package com.hackaton.controller;

import com.hackaton.dto.activity.ActivityResponse;
import com.hackaton.dto.admin.ChangeUserRoleRequest;
import com.hackaton.dto.admin.UserResponse;
import com.hackaton.dto.group.GroupResponse;
import com.hackaton.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> changeUserRole(@PathVariable Long id,
                                                        @Valid @RequestBody ChangeUserRoleRequest request) {
        return ResponseEntity.ok(adminService.changeUserRole(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/groups")
    public ResponseEntity<List<GroupResponse>> getAllGroups() {
        return ResponseEntity.ok(adminService.getAllGroups());
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        adminService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/activities")
    public ResponseEntity<List<ActivityResponse>> getAllActivities() {
        return ResponseEntity.ok(adminService.getAllActivities());
    }

    @DeleteMapping("/activities/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        adminService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }
}
