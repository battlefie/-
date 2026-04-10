package com.studyabroad.service;

import com.studyabroad.entity.Application;
import com.studyabroad.entity.Student;
import com.studyabroad.entity.User;
import com.studyabroad.repository.ApplicationRepository;
import com.studyabroad.repository.StudentRepository;
import com.studyabroad.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 申请服务类
 */
@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PermissionService permissionService;

    public ApplicationService(ApplicationRepository applicationRepository, 
                            UserRepository userRepository,
                            StudentRepository studentRepository,
                            PermissionService permissionService) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public Application createApplication(Application application) {
        if (application.getStudent() == null || application.getStudent().getId() == null) {
            throw new RuntimeException("请先创建学生并选择申请人");
        }

        Student student = studentRepository.findById(application.getStudent().getId())
                .orElseThrow(() -> new RuntimeException("申请人不存在，请先创建学生信息"));

                application.setStudent(student);

        User currentUser = permissionService.getCurrentUser();
        boolean isAdmin = permissionService.isAdmin();
        boolean isCounselor = permissionService.isCounselor();
        boolean isWriter = permissionService.isWriter();

        if (!isAdmin) {
            if (isWriter) {
                if (student.getWriter() == null || currentUser == null ||
                        !student.getWriter().getId().equals(currentUser.getId())) {
                    throw new RuntimeException("只能为自己负责的学生创建申请");
                }
            } else if (isCounselor) {
                if (student.getCounselor() == null || currentUser == null ||
                        !student.getCounselor().getId().equals(currentUser.getId())) {
                    throw new RuntimeException("只能为自己负责的学生创建申请");
                }
            } else {
                throw new RuntimeException("没有权限创建申请");
            }
        }

        // 业务规则：申请的文案必须与学生的文案一致
        if (student.getWriter() != null) {
            application.setWriter(student.getWriter());
        } else {
            throw new RuntimeException("该学生尚未分配文案，无法创建申请");
        }
        
        // 检查咨询顾问信息是否存在
        if (application.getCounselor() != null && application.getCounselor().getId() != null) {
            User counselor = userRepository.findById(application.getCounselor().getId()).orElse(null);
            if (counselor != null) {
                application.setCounselor(counselor);
            }
        } else if (student.getCounselor() != null) {
            application.setCounselor(student.getCounselor());
        }
        
        // 如果学生没有文案，才允许手动设置文案（但通常应该先给学生分配文案）
        // 这种情况不应该发生，因为业务规则要求学生必须有文案
        if (application.getWriter() == null && application.getStudent() != null && application.getStudent().getWriter() == null) {
            // 如果请求中指定了文案，可以使用（但最好先给学生分配文案）
            // 这里暂时允许，但实际业务中应该先给学生分配文案
        }
        
        return applicationRepository.save(application);
    }

    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("申请信息不存在"));
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAllByOrderByCreateTimeDesc();
    }

    /**
     * 分页获取所有申请
     */
    public Page<Application> getApplications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return applicationRepository.findAll(pageable);
    }

    public List<Application> getApplicationsByUserId(Long userId) {
        return applicationRepository.findByStudentId(userId);
    }

    /**
     * 分页获取指定学生的申请
     */
    public Page<Application> getApplicationsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return applicationRepository.findByStudentId(userId, pageable);
    }

    public List<Application> getApplicationsByUniversityName(String universityName) {
        return applicationRepository.findByUniversityName(universityName);
    }

    public List<Application> getApplicationsByStatus(Application.ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    public List<Application> getApplicationsByCounselorId(Long counselorId) {
        return applicationRepository.findByCounselor(userRepository.findById(counselorId).orElse(null));
    }

    /**
     * 分页获取咨询顾问的申请
     */
    public Page<Application> getApplicationsByCounselorId(Long counselorId, int page, int size) {
        User counselor = userRepository.findById(counselorId).orElse(null);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return applicationRepository.findByCounselor(counselor, pageable);
    }

    /**
     * 根据文案ID获取申请（基于学生的writer_id，符合业务规则：文案负责申请）
     * 业务规则：一个学生只能被一个文案负责，文案负责该学生的所有申请
     */
    public List<Application> getApplicationsByWriterId(Long writerId) {
        // 使用新的查询方法，基于学生的writer_id查询
        return applicationRepository.findByStudentWriterId(writerId);
    }

    /**
     * 分页获取文案的申请（基于学生的writer_id）
     * 业务规则：一个学生只能被一个文案负责，文案负责该学生的所有申请
     */
    public Page<Application> getApplicationsByWriterId(Long writerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        // 使用新的查询方法，基于学生的writer_id查询
        return applicationRepository.findByStudentWriterIdPage(writerId, pageable);
    }

    @Transactional
    public Application updateApplication(Long id, Application applicationDetails) {
        Application application = getApplicationById(id);

        User currentUser = permissionService.getCurrentUser();
        boolean isAdmin = permissionService.isAdmin();
        boolean isCounselor = permissionService.isCounselor();
        boolean isWriter = permissionService.isWriter();

        if (!isAdmin) {
            if (isWriter) {
                if (application.getStudent() == null || application.getStudent().getWriter() == null ||
                        currentUser == null ||
                        !application.getStudent().getWriter().getId().equals(currentUser.getId())) {
                    throw new RuntimeException("没有权限修改此申请");
                }
            } else if (isCounselor) {
                if (application.getStudent() == null || application.getStudent().getCounselor() == null ||
                        currentUser == null ||
                        !application.getStudent().getCounselor().getId().equals(currentUser.getId())) {
                    throw new RuntimeException("没有权限修改此申请");
                }
            } else {
                throw new RuntimeException("没有权限修改申请");
            }
        }

        if (!isAdmin && applicationDetails.getStudent() != null && applicationDetails.getStudent().getId() != null &&
                !applicationDetails.getStudent().getId().equals(application.getStudent().getId())) {
            throw new RuntimeException("没有权限修改申请人");
        }

        // 如果学生信息被更新，重新加载学生信息
        if (applicationDetails.getStudent() != null && applicationDetails.getStudent().getId() != null) {
            Student student = studentRepository.findById(applicationDetails.getStudent().getId()).orElse(null);
            if (student != null) {
                application.setStudent(student);
                // 业务规则：申请的文案必须与学生的文案一致
                if (student.getWriter() != null) {
                    application.setWriter(student.getWriter());
                }
            }
        } else {
            // 如果没有更新学生，但需要确保文案与学生一致
            if (application.getStudent() != null && application.getStudent().getWriter() != null) {
                // 重新加载学生信息，确保获取最新的文案信息
                Student currentStudent = studentRepository.findById(application.getStudent().getId()).orElse(null);
                if (currentStudent != null && currentStudent.getWriter() != null) {
                    application.setWriter(currentStudent.getWriter());
                }
            }
        }
        
        if (applicationDetails.getMajor() != null) application.setMajor(applicationDetails.getMajor());
        if (applicationDetails.getDegreeType() != null) application.setDegreeType(applicationDetails.getDegreeType());
        if (applicationDetails.getStatus() != null) application.setStatus(applicationDetails.getStatus());
        if (applicationDetails.getApplicationDate() != null) application.setApplicationDate(applicationDetails.getApplicationDate());
        if (applicationDetails.getUniversityName() != null) application.setUniversityName(applicationDetails.getUniversityName());
        if (applicationDetails.getUniversityEmail() != null) application.setUniversityEmail(applicationDetails.getUniversityEmail());
        if (applicationDetails.getUniversityEmailPassword() != null) application.setUniversityEmailPassword(applicationDetails.getUniversityEmailPassword());
        if (applicationDetails.getCountry() != null) application.setCountry(applicationDetails.getCountry());
        if (applicationDetails.getNotes() != null) application.setNotes(applicationDetails.getNotes());
        if (applicationDetails.getCounselor() != null) application.setCounselor(applicationDetails.getCounselor());
        // 不直接更新Writer，因为Writer应该始终从学生获取
        // if (applicationDetails.getWriter() != null) application.setWriter(applicationDetails.getWriter());
        
        // 签证信息
        if (applicationDetails.getVisaSubmissionDate() != null) application.setVisaSubmissionDate(applicationDetails.getVisaSubmissionDate());
        if (applicationDetails.getInterviewDate() != null) application.setInterviewDate(applicationDetails.getInterviewDate());
        if (applicationDetails.getFingerprintCollectionDate() != null) application.setFingerprintCollectionDate(applicationDetails.getFingerprintCollectionDate());
        if (applicationDetails.getMedicalExamDate() != null) application.setMedicalExamDate(applicationDetails.getMedicalExamDate());
        if (applicationDetails.getVisaApprovedDate() != null) application.setVisaApprovedDate(applicationDetails.getVisaApprovedDate());
        if (applicationDetails.getVisaRejectedDate() != null) application.setVisaRejectedDate(applicationDetails.getVisaRejectedDate());
        
        // 后续跟踪
        if (applicationDetails.getDepartureDate() != null) application.setDepartureDate(applicationDetails.getDepartureDate());
        if (applicationDetails.getAirportPickupAccommodation() != null) application.setAirportPickupAccommodation(applicationDetails.getAirportPickupAccommodation());
        if (applicationDetails.getFollowUpStatus() != null) application.setFollowUpStatus(applicationDetails.getFollowUpStatus());
        if (applicationDetails.getArrivalStatus() != null) application.setArrivalStatus(applicationDetails.getArrivalStatus());
        
        // 状态链接
        if (applicationDetails.getStatusUrl() != null) application.setStatusUrl(applicationDetails.getStatusUrl());
        
        return applicationRepository.save(application);
    }

    @Transactional
    public void deleteApplication(Long id) {
        applicationRepository.deleteById(id);
    }

    /**
     * 为学生创建申请（简化版本）
     */
    @Transactional
    public Application createStudentApplication(com.studyabroad.controller.ApplicationController.StudentApplicationRequest request, String token) {
        // 这里简化实现，直接使用现有的createApplication方法
        // 在实际应用中，应该根据token解析用户信息，获取学生ID
        
        // 创建申请对象
        Application application = new Application();
        
        // 设置学生（这里需要根据实际需求获取学生ID）
        // 暂时使用一个默认的学生ID，实际应该从token中解析
        Student student = studentRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("学生信息不存在"));
        application.setStudent(student);
        
        // 设置大学名称和国家
        application.setUniversityName(request.getUniversityName());
        application.setCountry(request.getCountry());
        
        // 设置其他字段
        application.setMajor(request.getMajor());
        application.setDegreeType(Application.DegreeType.valueOf(request.getDegreeType()));
        application.setStatus(Application.ApplicationStatus.DRAFT);
        
        if (request.getApplicationDate() != null && !request.getApplicationDate().isEmpty()) {
            application.setApplicationDate(LocalDate.parse(request.getApplicationDate()));
        }
        
        // application.setSemester(request.getSemester()); // 字段已移除
        application.setNotes(request.getNotes());
        
        return applicationRepository.save(application);
    }
}

