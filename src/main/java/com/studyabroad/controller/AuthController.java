package com.studyabroad.controller;

import com.studyabroad.dto.ApiResponse;
import com.studyabroad.dto.LoginRequest;
import com.studyabroad.dto.RegisterRequest;
import com.studyabroad.service.UserService;
import com.studyabroad.service.PermissionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PermissionService permissionService;

    public AuthController(UserService userService, PermissionService permissionService) {
        this.userService = userService;
        this.permissionService = permissionService;
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody RegisterRequest request, Authentication authentication) {
        try {
            // 检查权限：只有管理员可以注册账号
            if (authentication == null || !permissionService.isAdmin()) {
                return ApiResponse.error("只有管理员可以注册账号");
            }
            
            Map<String, Object> result = userService.register(request);
            return ApiResponse.success("注册成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            Map<String, Object> result = userService.login(request);
            return ApiResponse.success("登录成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

