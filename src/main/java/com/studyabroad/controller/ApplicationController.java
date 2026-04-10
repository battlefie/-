package com.studyabroad.controller;

import com.studyabroad.dto.ApiResponse;
import com.studyabroad.entity.Application;
import com.studyabroad.entity.Document;
import com.studyabroad.entity.Student;
import com.studyabroad.repository.StudentRepository;
import com.studyabroad.service.ApplicationService;
import com.studyabroad.service.DocumentService;
import com.studyabroad.service.PermissionService;
import com.studyabroad.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 申请控制器
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final DocumentService documentService;
    private final PermissionService permissionService;
    private final FileStorageService fileStorageService;
    private final StudentRepository studentRepository;

    public ApplicationController(ApplicationService applicationService, DocumentService documentService, PermissionService permissionService, FileStorageService fileStorageService, StudentRepository studentRepository) {
        this.applicationService = applicationService;
        this.documentService = documentService;
        this.permissionService = permissionService;
        this.fileStorageService = fileStorageService;
        this.studentRepository = studentRepository;
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
            @RequestParam(required = false) String universityName,
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
                    if (studentId != null) {
                        return ApiResponse.success(applicationService.getApplicationsByUserId(studentId, page, size));
                    }
                    return ApiResponse.success(applicationService.getApplications(page, size));
                } else if (permissionService.isCounselor()) {
                    // 咨询顾问只能查看自己学生的申请
                    Long currentCounselorId = permissionService.getCurrentUser().getId();
                    // 如果指定了studentId，验证该学生是否属于当前顾问
                    if (studentId != null) {
                        Student student = studentRepository.findById(studentId).orElse(null);
                        if (student == null) {
                            return ApiResponse.error("学生不存在");
                        }
                        if (student.getCounselor() == null || !student.getCounselor().getId().equals(currentCounselorId)) {
                            return ApiResponse.error("您只能查看自己的学生申请");
                        }
                        // 验证通过，返回该学生的申请
                        return ApiResponse.success(applicationService.getApplicationsByUserId(studentId, page, size));
                    }
                    return ApiResponse.success(applicationService.getApplicationsByCounselorId(currentCounselorId, page, size));
                } else if (permissionService.isWriter()) {
                    // 文案只能查看自己负责的申请
                    Long currentWriterId = permissionService.getCurrentUser().getId();
                    // 如果指定了studentId，验证该学生是否属于当前文案
                    if (studentId != null) {
                        Student student = studentRepository.findById(studentId).orElse(null);
                        if (student == null) {
                            return ApiResponse.error("学生不存在");
                        }
                        if (student.getWriter() == null || !student.getWriter().getId().equals(currentWriterId)) {
                            return ApiResponse.error("您只能查看自己负责的学生申请");
                        }
                        // 验证通过，返回该学生的申请
                        return ApiResponse.success(applicationService.getApplicationsByUserId(studentId, page, size));
                    }
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
                } else if (universityName != null && !universityName.trim().isEmpty()) {
                    applications = applicationService.getApplicationsByUniversityName(universityName.trim());
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
                // 如果指定了studentId，只返回该学生的申请（需要验证该学生是否属于当前顾问）
                if (studentId != null) {
                    // 先验证该学生是否属于当前顾问
                    Student student = studentRepository.findById(studentId).orElse(null);
                    if (student == null) {
                        return ApiResponse.error("学生不存在");
                    }
                    if (student.getCounselor() == null || !student.getCounselor().getId().equals(currentCounselorId)) {
                        return ApiResponse.error("您只能查看自己的学生申请");
                    }
                    // 验证通过，返回该学生的申请
                    applications = applicationService.getApplicationsByUserId(studentId);
                } else {
                    // 如果没有指定studentId，返回该顾问所有学生的申请
                    applications = applicationService.getApplicationsByCounselorId(currentCounselorId);
                }
            } else if (permissionService.isWriter()) {
                // 文案只能查看自己负责的申请
                Long currentWriterId = permissionService.getCurrentUser().getId();
                // 如果指定了studentId，只返回该学生的申请（需要验证该学生是否属于当前文案）
                if (studentId != null) {
                    // 先验证该学生是否属于当前文案
                    Student student = studentRepository.findById(studentId).orElse(null);
                    if (student == null) {
                        return ApiResponse.error("学生不存在");
                    }
                    if (student.getWriter() == null || !student.getWriter().getId().equals(currentWriterId)) {
                        return ApiResponse.error("您只能查看自己负责的学生申请");
                    }
                    // 验证通过，返回该学生的申请
                    applications = applicationService.getApplicationsByUserId(studentId);
                } else {
                    // 如果没有指定studentId，返回该文案所有负责的申请
                    applications = applicationService.getApplicationsByWriterId(currentWriterId);
                }
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

    /**
     * 上传申请文件（创建Document记录）
     */
    @PostMapping("/{id}/upload")
    public ApiResponse<Document> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ApiResponse.error("请选择要上传的文件");
            }
            
            Application application = applicationService.getApplicationById(id);
            
            // 检查文件大小
            long fileSize = file.getSize();
            if (fileSize > 52428800) { // 50MB
                return ApiResponse.error("文件大小超过限制（最大50MB）");
            }
            
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            
            // 创建Document对象
            Document document = new Document();
            document.setApplication(application);
            
            // 根据配置选择存储方式
            String storageType = fileStorageService.getStorageType();
            
            if ("database".equals(storageType)) {
                // 数据库存储：将文件内容读取为字节数组
                byte[] fileContent = file.getBytes();
                document.setFilePath(null); // 数据库存储时不需要文件路径
                document.setFileName(fileName);
                document.setFileContent(fileContent);
                document.setFileSize(fileSize);
                document.setFileContentType(contentType);
            } else {
                // 文件系统存储
                String filePath = fileStorageService.storeFile(file, id);
                document.setFilePath(filePath);
                document.setFileName(fileName);
                document.setFileContent(null); // 文件系统存储时不需要文件内容
                document.setFileSize(fileSize);
                document.setFileContentType(contentType);
            }
            
            Document savedDocument = documentService.createDocument(document);
            
            return ApiResponse.success("文件上传成功（文件大小: " + (fileSize / 1024.0 / 1024.0) + "MB，存储方式: " + storageType + "）", savedDocument);
        } catch (org.springframework.web.multipart.MaxUploadSizeExceededException e) {
            return ApiResponse.error("文件大小超过限制（最大50MB）");
        } catch (Exception e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取申请的所有文档
     */
    @GetMapping("/{id}/documents")
    public ApiResponse<List<Document>> getApplicationDocuments(@PathVariable Long id) {
        try {
            List<Document> documents = documentService.getDocumentsByApplicationId(id);
            return ApiResponse.success(documents);
        } catch (Exception e) {
            return ApiResponse.error("获取文档列表失败: " + e.getMessage());
        }
    }

    /**
     * 下载申请文件（从Document表）
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            // 获取申请的第一个文档（保持向后兼容）
            List<Document> documents = documentService.getDocumentsByApplicationId(id);
            if (documents == null || documents.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // 使用最新的文档
            Document document = documents.get(documents.size() - 1);
            
            String storageType = fileStorageService.getStorageType();
            Resource resource;
            String contentType;
            
            if ("database".equals(storageType)) {
                // 数据库存储：从数据库读取文件内容
                if (document.getFileContent() == null || document.getFileContent().length == 0) {
                    return ResponseEntity.notFound().build();
                }
                
                // 创建临时文件或使用ByteArrayResource
                org.springframework.core.io.ByteArrayResource byteArrayResource = 
                    new org.springframework.core.io.ByteArrayResource(document.getFileContent());
                
                resource = byteArrayResource;
                contentType = document.getFileContentType() != null 
                    ? document.getFileContentType() 
                    : "application/octet-stream";
            } else {
                // 文件系统存储：从文件系统读取
                if (document.getFilePath() == null || document.getFilePath().isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                Path filePath = fileStorageService.getFilePath(document.getFilePath());
                resource = new UrlResource(filePath.toUri());
                
                if (!resource.exists() || !resource.isReadable()) {
                    return ResponseEntity.notFound().build();
                }
                
                contentType = "application/octet-stream";
                try {
                    contentType = Files.probeContentType(filePath);
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                } catch (IOException e) {
                    // 使用默认类型
                }
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + (document.getFileName() != null ? document.getFileName() : "file") + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 下载指定文档
     */
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId) {
        try {
            Document document = documentService.getDocumentById(documentId)
                    .orElseThrow(() -> new RuntimeException("文档不存在"));
            
            String storageType = fileStorageService.getStorageType();
            Resource resource;
            String contentType;
            
            if ("database".equals(storageType)) {
                // 数据库存储：从数据库读取文件内容
                if (document.getFileContent() == null || document.getFileContent().length == 0) {
                    return ResponseEntity.notFound().build();
                }
                
                org.springframework.core.io.ByteArrayResource byteArrayResource = 
                    new org.springframework.core.io.ByteArrayResource(document.getFileContent());
                
                resource = byteArrayResource;
                contentType = document.getFileContentType() != null 
                    ? document.getFileContentType() 
                    : "application/octet-stream";
            } else {
                // 文件系统存储：从文件系统读取
                if (document.getFilePath() == null || document.getFilePath().isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                Path filePath = fileStorageService.getFilePath(document.getFilePath());
                resource = new UrlResource(filePath.toUri());
                
                if (!resource.exists() || !resource.isReadable()) {
                    return ResponseEntity.notFound().build();
                }
                
                contentType = "application/octet-stream";
                try {
                    contentType = Files.probeContentType(filePath);
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                } catch (IOException e) {
                    // 使用默认类型
                }
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + (document.getFileName() != null ? document.getFileName() : "file") + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除申请文件（删除所有文档，保持向后兼容）
     */
    @DeleteMapping("/{id}/file")
    public ApiResponse<Void> deleteFile(@PathVariable Long id) {
        try {
            // 删除申请的所有文档
            documentService.deleteDocumentsByApplicationId(id);
            return ApiResponse.success("文件删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定文档
     */
    @DeleteMapping("/documents/{documentId}")
    public ApiResponse<Void> deleteDocument(@PathVariable Long documentId) {
        try {
            Document document = documentService.getDocumentById(documentId)
                    .orElseThrow(() -> new RuntimeException("文档不存在"));
            
            // 如果是文件系统存储，删除文件
            String storageType = fileStorageService.getStorageType();
            if (!"database".equals(storageType) && document.getFilePath() != null) {
                try {
                    fileStorageService.deleteFile(document.getFilePath());
                } catch (IOException e) {
                    // 忽略删除错误，继续删除数据库记录
                }
            }
            
            documentService.deleteDocument(documentId);
            return ApiResponse.success("文档删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error("文档删除失败: " + e.getMessage());
        }
    }
}

