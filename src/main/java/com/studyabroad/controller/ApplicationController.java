package com.studyabroad.controller;

import com.studyabroad.dto.ApiResponse;
import com.studyabroad.entity.Application;
import com.studyabroad.service.ApplicationService;
import com.studyabroad.service.PermissionService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 申请控制器
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final PermissionService permissionService;

    public ApplicationController(ApplicationService applicationService, PermissionService permissionService) {
        this.applicationService = applicationService;
        this.permissionService = permissionService;
    }

    @PostMapping
    public ApiResponse<Application> createApplication(@RequestBody Application application) {
        try {
            Application createdApplication = applicationService.createApplication(application);
            return ApiResponse.success("申请创建成功", createdApplication);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<Application> getApplicationById(@PathVariable Long id) {
        try {
            Application application = applicationService.getApplicationById(id);
            return ApiResponse.success(application);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<?> getApplications(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long universityId,
            @RequestParam(required = false) Long counselorId,
            @RequestParam(required = false) Application.ApplicationStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            // 如果提供了分页参数，返回分页数据
            if (page != null && size != null) {
                // 权限控制
                if (permissionService.isAdmin()) {
                    // 管理员可以查看所有申请
                    return ApiResponse.success(applicationService.getApplications(page, size));
                } else if (permissionService.isCounselor()) {
                    // 咨询顾问只能查看自己学生的申请
                    Long currentCounselorId = permissionService.getCurrentUser().getId();
                    return ApiResponse.success(applicationService.getApplicationsByCounselorId(currentCounselorId, page, size));
                } else if (permissionService.isWriter()) {
                    // 文案只能查看自己负责的申请
                    Long currentWriterId = permissionService.getCurrentUser().getId();
                    return ApiResponse.success(applicationService.getApplicationsByWriterId(currentWriterId, page, size));
                } else {
                    return ApiResponse.error("您没有权限访问申请信息");
                }
            }
            
            // 否则返回列表数据（保持向后兼容）
            List<Application> applications;
            
            // 权限控制：管理员可以查看所有，咨询顾问只能查看自己的，文案只能查看自己负责的
            if (permissionService.isAdmin()) {
                // 管理员可以查看所有申请
                if (studentId != null) {
                    applications = applicationService.getApplicationsByUserId(studentId);
                } else if (universityId != null) {
                    applications = applicationService.getApplicationsByUniversityName(universityId.toString());
                } else if (counselorId != null) {
                    applications = applicationService.getApplicationsByCounselorId(counselorId);
                } else if (status != null) {
                    applications = applicationService.getApplicationsByStatus(status);
                } else {
                    applications = applicationService.getAllApplications();
                }
            } else if (permissionService.isCounselor()) {
                // 咨询顾问只能查看自己学生的申请
                Long currentCounselorId = permissionService.getCurrentUser().getId();
                if (counselorId != null && !counselorId.equals(currentCounselorId)) {
                    return ApiResponse.error("您只能查看自己的学生申请");
                }
                applications = applicationService.getApplicationsByCounselorId(currentCounselorId);
            } else if (permissionService.isWriter()) {
                // 文案只能查看自己负责的申请
                Long currentWriterId = permissionService.getCurrentUser().getId();
                applications = applicationService.getApplicationsByWriterId(currentWriterId);
            } else {
                return ApiResponse.error("您没有权限访问申请信息");
            }
            
            return ApiResponse.success(applications);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Application> updateApplication(@PathVariable Long id, @RequestBody Application application) {
        try {
            Application updatedApplication = applicationService.updateApplication(id, application);
            return ApiResponse.success("申请更新成功", updatedApplication);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteApplication(@PathVariable Long id) {
        try {
            applicationService.deleteApplication(id);
            return ApiResponse.success("申请删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 专门为学生设计的简化申请创建API
    @PostMapping("/student/create")
    public ApiResponse<Application> createStudentApplication(@RequestBody StudentApplicationRequest request,
                                                           @RequestHeader("Authorization") String authHeader) {
        try {
            // 从Authorization头中提取token
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            
            Application createdApplication = applicationService.createStudentApplication(request, token);
            return ApiResponse.success("申请创建成功", createdApplication);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 学生申请请求DTO
    public static class StudentApplicationRequest {
        private String universityName;
        private String country;
        private String major;
        private String degreeType;
        private String applicationDate;
        private String notes;

        // Getters and Setters
        public String getUniversityName() { return universityName; }
        public void setUniversityName(String universityName) { this.universityName = universityName; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getMajor() { return major; }
        public void setMajor(String major) { this.major = major; }
        
        public String getDegreeType() { return degreeType; }
        public void setDegreeType(String degreeType) { this.degreeType = degreeType; }
        
        // public String getSemester() { return semester; }
        // public void setSemester(String semester) { this.semester = semester; }
        
        public String getApplicationDate() { return applicationDate; }
        public void setApplicationDate(String applicationDate) { this.applicationDate = applicationDate; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}

