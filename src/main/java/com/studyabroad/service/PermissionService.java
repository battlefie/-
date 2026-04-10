package com.studyabroad.service;

import com.studyabroad.entity.User;
import com.studyabroad.entity.Application;
import com.studyabroad.repository.UserRepository;
import com.studyabroad.repository.ApplicationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限服务类
 * 处理用户权限检查和数据过滤
 */
@Service
public class PermissionService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    public PermissionService(UserRepository userRepository, ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
    }

    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * 检查当前用户是否为超级管理员
     */
    public boolean isSuperAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getRole() == User.UserRole.SUPER_ADMIN;
    }

    /**
     * 检查当前用户是否为具备管理员权限的角色（超级管理员或普通管理员）
     */
    public boolean isAdmin() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        User.UserRole role = currentUser.getRole();
        return role == User.UserRole.SUPER_ADMIN || role == User.UserRole.ADMIN;
    }

    /**
     * 检查当前用户是否为咨询顾问
     */
    public boolean isCounselor() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getRole() == User.UserRole.COUNSELOR;
    }

    /**
     * 检查当前用户是否为文案
     */
    public boolean isWriter() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getRole() == User.UserRole.WRITER;
    }

    /**
     * 检查当前用户是否可以访问指定用户
     * 管理员可以访问所有用户，咨询顾问只能访问自己指导的学生
     */
    public boolean canAccessUser(Long userId) {
        if (isSuperAdmin()) {
            return true; // 超级管理员可以访问所有用户
        }
        
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getId().equals(userId);
    }

    /**
     * 获取当前用户可以访问的用户列表
     * 管理员返回所有用户，咨询顾问和文案只能访问自己
     */
    public List<User> getAccessibleUsers() {
        if (isAdmin()) {
            return userRepository.findAll();
        }
        
        if (isCounselor() || isWriter()) {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                // 咨询顾问和文案只能访问自己的用户信息
                return List.of(currentUser);
            }
        }
        
        return List.of(); // 返回空列表
    }

    /**
     * 检查当前用户是否可以访问指定申请
     * 管理员可以访问所有申请，咨询顾问只能访问自己学生的申请
     */
    public boolean canAccessApplication(Long applicationId) {
        if (isAdmin()) {
            return true; // 管理员可以访问所有申请
        }
        
        if (isCounselor()) {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return false;
            }
            
            // 通过申请ID查找学生，然后检查权限
            // 这里需要根据实际的Application实体结构调整
            return true; // 暂时返回true，实际实现需要根据Application实体结构
        }
        
        return false;
    }

    /**
     * 获取当前用户可以访问的申请列表
     * 管理员返回所有申请，咨询顾问只返回自己学生的申请
     */
    public List<Application> getAccessibleApplications() {
        if (isAdmin()) {
            return applicationRepository.findAll();
        }
        
        if (isCounselor()) {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                return applicationRepository.findByCounselor(currentUser);
            }
        }
        
        return List.of(); // 返回空列表
    }
}
