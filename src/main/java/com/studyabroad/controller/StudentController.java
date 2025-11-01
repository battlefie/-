package com.studyabroad.controller;

import com.studyabroad.dto.ApiResponse;
import com.studyabroad.dto.CreateStudentRequest;
import com.studyabroad.entity.Student;
import com.studyabroad.service.StudentService;
import com.studyabroad.service.PermissionService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生控制器
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final PermissionService permissionService;

    public StudentController(StudentService studentService, PermissionService permissionService) {
        this.studentService = studentService;
        this.permissionService = permissionService;
    }

    /**
     * 创建学生
     */
    @PostMapping
    public ApiResponse<Student> createStudent(@RequestBody CreateStudentRequest request) {
        try {
            if (!permissionService.isAdmin() && !permissionService.isCounselor()) {
                return ApiResponse.error("没有权限创建学生");
            }
            
            // 如果是咨询顾问创建学生，自动分配给自己
            if (permissionService.isCounselor() && !permissionService.isAdmin()) {
                request.setCounselorId(permissionService.getCurrentUser().getId());
            }
            
            Student createdStudent = studentService.createStudent(request);
            return ApiResponse.success("学生创建成功", createdStudent);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取学生
     */
    @GetMapping("/{id}")
    public ApiResponse<Student> getStudentById(@PathVariable Long id) {
        try {
            Student student = studentService.getStudentById(id);
            
            // 检查权限
            if (!permissionService.isAdmin() && permissionService.isCounselor()) {
                if (student.getCounselor() == null || 
                    !student.getCounselor().getId().equals(permissionService.getCurrentUser().getId())) {
                    return ApiResponse.error("没有权限查看此学生信息");
                }
            }
            
            return ApiResponse.success(student);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有学生
     */
    @GetMapping
    public ApiResponse<?> getAllStudents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        try {
            // 如果提供了页码和大小，使用分页查询
            if (page >= 0 && size > 0) {
                Page<Student> studentPage;
                if (name != null && !name.trim().isEmpty()) {
                    studentPage = studentService.searchStudentsByName(name, page, size);
                } else {
                    studentPage = studentService.getStudents(page, size);
                }
                
                // 构建分页响应
                java.util.Map<String, Object> result = new java.util.HashMap<>();
                result.put("content", studentPage.getContent());
                result.put("totalElements", studentPage.getTotalElements());
                result.put("totalPages", studentPage.getTotalPages());
                result.put("currentPage", studentPage.getNumber());
                result.put("pageSize", studentPage.getSize());
                result.put("hasNext", studentPage.hasNext());
                result.put("hasPrevious", studentPage.hasPrevious());
                
                return ApiResponse.success(result);
            } else {
                // 使用非分页查询（保持向后兼容）
                List<Student> students;
                if (name != null && !name.trim().isEmpty()) {
                    students = studentService.searchStudentsByName(name);
                } else {
                    students = studentService.getAllStudents();
                }
                return ApiResponse.success(students);
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据咨询顾问获取学生
     */
    @GetMapping("/counselor/{counselorId}")
    public ApiResponse<List<Student>> getStudentsByCounselor(@PathVariable Long counselorId) {
        try {
            if (!permissionService.isAdmin()) {
                return ApiResponse.error("只有管理员可以查看指定咨询顾问的学生");
            }
            
            List<Student> students = studentService.getStudentsByCounselor(permissionService.getCurrentUser());
            return ApiResponse.success(students);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据文案获取学生
     */
    @GetMapping("/writer/{writerId}")
    public ApiResponse<List<Student>> getStudentsByWriter(@PathVariable Long writerId) {
        try {
            if (!permissionService.isAdmin()) {
                return ApiResponse.error("只有管理员可以查看指定文案的学生");
            }
            
            List<Student> students = studentService.getStudentsByWriter(permissionService.getCurrentUser());
            return ApiResponse.success(students);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新学生信息
     */
    @PutMapping("/{id}")
    public ApiResponse<Student> updateStudent(@PathVariable Long id, @RequestBody CreateStudentRequest request) {
        try {
            Student updatedStudent = studentService.updateStudent(id, request);
            return ApiResponse.success("学生信息更新成功", updatedStudent);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除学生
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            return ApiResponse.success("学生删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 为咨询顾问分配学生
     */
    @PostMapping("/{studentId}/assign-counselor/{counselorId}")
    public ApiResponse<Student> assignStudentToCounselor(@PathVariable Long studentId, @PathVariable Long counselorId) {
        try {
            Student student = studentService.assignStudentToCounselor(studentId, counselorId);
            return ApiResponse.success("学生分配成功", student);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 为文案分配学生
     */
    @PostMapping("/{studentId}/assign-writer/{writerId}")
    public ApiResponse<Student> assignStudentToWriter(@PathVariable Long studentId, @PathVariable Long writerId) {
        try {
            Student student = studentService.assignStudentToWriter(studentId, writerId);
            return ApiResponse.success("学生分配给文案成功", student);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
