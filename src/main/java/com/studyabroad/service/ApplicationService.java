package com.studyabroad.service;

import com.studyabroad.entity.Application;
import com.studyabroad.entity.Student;
import com.studyabroad.entity.University;
import com.studyabroad.entity.User;
import com.studyabroad.repository.ApplicationRepository;
import com.studyabroad.repository.StudentRepository;
import com.studyabroad.repository.UniversityRepository;
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
    private final UniversityRepository universityRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public ApplicationService(ApplicationRepository applicationRepository, 
                            UniversityRepository universityRepository,
                            UserRepository userRepository,
                            StudentRepository studentRepository) {
        this.applicationRepository = applicationRepository;
        this.universityRepository = universityRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public Application createApplication(Application application) {
        // 检查学生信息是否存在
        if (application.getStudent() != null && application.getStudent().getId() != null) {
            Student student = studentRepository.findById(application.getStudent().getId()).orElse(null);
            if (student != null) {
                // 确认学生关联
                application.setStudent(student);
            }
        }
        
        // 检查咨询顾问信息是否存在
        if (application.getCounselor() != null && application.getCounselor().getId() != null) {
            User counselor = userRepository.findById(application.getCounselor().getId()).orElse(null);
            if (counselor != null) {
                application.setCounselor(counselor);
            }
        }
        
        // 检查文案信息是否存在
        if (application.getWriter() != null && application.getWriter().getId() != null) {
            User writer = userRepository.findById(application.getWriter().getId()).orElse(null);
            if (writer != null) {
                application.setWriter(writer);
            }
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

    public List<Application> getApplicationsByWriterId(Long writerId) {
        return applicationRepository.findByWriter(userRepository.findById(writerId).orElse(null));
    }

    /**
     * 分页获取文案的申请
     */
    public Page<Application> getApplicationsByWriterId(Long writerId, int page, int size) {
        User writer = userRepository.findById(writerId).orElse(null);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return applicationRepository.findByWriter(writer, pageable);
    }

    @Transactional
    public Application updateApplication(Long id, Application applicationDetails) {
        Application application = getApplicationById(id);
        
        if (applicationDetails.getMajor() != null) application.setMajor(applicationDetails.getMajor());
        if (applicationDetails.getDegreeType() != null) application.setDegreeType(applicationDetails.getDegreeType());
        if (applicationDetails.getStatus() != null) application.setStatus(applicationDetails.getStatus());
        if (applicationDetails.getApplicationDate() != null) application.setApplicationDate(applicationDetails.getApplicationDate());
        if (applicationDetails.getUniversityName() != null) application.setUniversityName(applicationDetails.getUniversityName());
        if (applicationDetails.getCountry() != null) application.setCountry(applicationDetails.getCountry());
        if (applicationDetails.getNotes() != null) application.setNotes(applicationDetails.getNotes());
        if (applicationDetails.getCounselor() != null) application.setCounselor(applicationDetails.getCounselor());
        if (applicationDetails.getWriter() != null) application.setWriter(applicationDetails.getWriter());
        
        // 签证信息
        if (applicationDetails.getVisaSubmissionDate() != null) application.setVisaSubmissionDate(applicationDetails.getVisaSubmissionDate());
        if (applicationDetails.getInterviewDate() != null) application.setInterviewDate(applicationDetails.getInterviewDate());
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
        application.setStatus(Application.ApplicationStatus.PENDING);
        
        if (request.getApplicationDate() != null && !request.getApplicationDate().isEmpty()) {
            application.setApplicationDate(LocalDate.parse(request.getApplicationDate()));
        }
        
        // application.setSemester(request.getSemester()); // 字段已移除
        application.setNotes(request.getNotes());
        
        return applicationRepository.save(application);
    }
}

