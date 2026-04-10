package com.studyabroad.controller;

import com.studyabroad.dto.ApiResponse;
import com.studyabroad.entity.User;
import com.studyabroad.service.UserService;
import com.studyabroad.service.PermissionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PermissionService permissionService;

    public UserController(UserService userService, PermissionService permissionService) {
        this.userService = userService;
        this.permissionService = permissionService;
    }

    @GetMapping("/me")
    public ApiResponse<User> getCurrentUser(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<List<User>> getAllUsers() {
        try {
            // 超级管理员可以查看所有用户
            if (permissionService.isSuperAdmin()) {
                List<User> users = userService.getAllUsers();
                return ApiResponse.success(users);
            }
            // 管理员也可以查看所有用户（用于填充下拉框等）
            if (permissionService.isAdmin()) {
                List<User> users = userService.getAllUsers();
                return ApiResponse.success(users);
            }
            return ApiResponse.error("没有权限查看用户列表");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有顾问和文案（用于下拉框选择）
     * 管理员、顾问和文案都可以访问
     */
    @GetMapping("/counselors-and-writers")
    public ApiResponse<List<User>> getCounselorsAndWriters() {
        try {
            List<User> counselors = userService.getUsersByRole(User.UserRole.COUNSELOR);
            List<User> writers = userService.getUsersByRole(User.UserRole.WRITER);
            List<User> result = new java.util.ArrayList<>();
            result.addAll(counselors);
            result.addAll(writers);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ApiResponse.success("用户信息更新成功", updatedUser);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ApiResponse.success("用户删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

