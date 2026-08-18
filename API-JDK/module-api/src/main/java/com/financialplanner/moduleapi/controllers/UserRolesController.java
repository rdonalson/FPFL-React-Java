package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.moduleauth.domain.service.UserRolesService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users/{userId}/roles")
public class UserRolesController {

    private final UserRolesService userRolesService;
    private final ApiResponseFactory responseFactory;

    public UserRolesController(UserRolesService userRolesService,
                               ApiResponseFactory responseFactory) {
        this.userRolesService = userRolesService;
        this.responseFactory = responseFactory;
    }

    // ------------------------------------------------------------
    // GET /users/{userId}/roles
    // ------------------------------------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<Set<String>>> getRoles(@PathVariable Long userId) {

        Set<String> roleNames = Set.copyOf(userRolesService.getRoleNamesForUser(userId));

        ApiResponse<Set<String>> body =
            responseFactory.success(roleNames, "Roles retrieved successfully");

        return ResponseEntity.ok(body);
    }

    // ------------------------------------------------------------
    // POST /users/{userId}/roles/{roleId}
    // ------------------------------------------------------------
    @PostMapping("/{roleId}")
    public ResponseEntity<ApiResponse<Void>> addRole(
        @PathVariable Long userId,
        @PathVariable Long roleId) {

        userRolesService.addRoleToUser(userId, roleId);

        ApiResponse<Void> body =
            responseFactory.success("Role added successfully");

        return ResponseEntity.ok(body);
    }

    // ------------------------------------------------------------
    // DELETE /users/{userId}/roles/{roleId}
    // ------------------------------------------------------------
    @DeleteMapping("/{roleId}")
    public ResponseEntity<ApiResponse<Void>> removeRole(
        @PathVariable Long userId,
        @PathVariable Long roleId) {

        int deleted = userRolesService.removeRoleFromUser(userId, roleId);

        ApiResponse<Void> body = (deleted == 0)
            ? responseFactory.success("Role was not assigned to user")
            : responseFactory.success("Role removed successfully");

        return ResponseEntity.ok(body);
    }

    // ------------------------------------------------------------
    // PUT /users/{userId}/roles
    // Body: { "roleIds": [1, 2, 3] }
    // ------------------------------------------------------------
    public static record ReplaceRolesRequest(@Valid List<Long> roleIds) {}

    @PutMapping
    public ResponseEntity<ApiResponse<Void>> replaceRoles(
        @PathVariable Long userId,
        @Valid @RequestBody ReplaceRolesRequest request) {

        userRolesService.replaceRolesForUser(userId, request.roleIds());

        ApiResponse<Void> body =
            responseFactory.success("Roles replaced successfully");

        return ResponseEntity.ok(body);
    }
}
