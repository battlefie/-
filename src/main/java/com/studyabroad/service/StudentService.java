package com.studyabroad.service;

import com.studyabroad.dto.CreateStudentRequest;
import com.studyabroad.entity.Student;
import com.studyabroad.entity.User;
import com.studyabroad.repository.StudentRepository;
import com.studyabroad.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学生服务类
 */
@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    public StudentService(StudentRepository studentRepository, UserRepository userRepository, PermissionService permissionService) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.permissionService = permissionService;
    }

    /**
     * 创建学生
     */
    public Student createStudent(CreateStudentRequest request) {
        Student student = new Student();
        student.setName(request.getName());
        student.setStudentSource(request.getStudentSource());
        student.setStatus(request.getStatus());
        student.setGender(request.getGender());
        student.setBirthDate(request.getBirthDate());
        student.setNationality(request.getNationality());
        student.setIdCard(request.getIdCard());
        student.setAddress(request.getAddress());
        student.setPhone(request.getPhone());
        student.setEmail(request.getEmail());
        student.setWechat(request.getWechat());
        student.setCurrentSchool(request.getCurrentSchool());
        student.setEnrollmentDate(request.getEnrollmentDate());
        student.setChannelSource(request.getChannelSource());
        student.setContractDate(request.getContractDate());
        student.setContractAmount(request.getContractAmount());
        student.setMajor(request.getMajor());
        student.setGpa(request.getGpa());
        student.setToeflScore(request.getToeflScore());
        student.setIeltsScore(request.getIeltsScore());
        student.setGreScore(request.getGreScore());
        student.setGmatScore(request.getGmatScore());
        student.setAwards(request.getAwards());
        student.setExperiences(request.getExperiences());
        student.setNotes(request.getNotes());
        
        // 设置咨询顾问和文案
        if (request.getCounselorId() != null) {
            student.setCounselor(userRepository.findById(request.getCounselorId()).orElse(null));
        }
        if (request.getWriterId() != null) {
            student.setWriter(userRepository.findById(request.getWriterId()).orElse(null));
        }
        
        return studentRepository.save(student);
    }

    /**
     * 根据ID获取学生
     */
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
    }
    
    /**
     * 根据邮箱获取学生
     */
    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
    }

    /**
     * 获取所有学生（根据权限过滤）
     */
    public List<Student> getAllStudents() {
        if (permissionService.isAdmin()) {
            return studentRepository.findAll();
        } else if (permissionService.isCounselor()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                return studentRepository.findByCounselor(currentUser);
            }
        } else if (permissionService.isWriter()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                return studentRepository.findByWriter(currentUser);
            }
        }
        return List.of();
    }

    /**
     * 分页获取学生（根据权限过滤）
     */
    public Page<Student> getStudents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (permissionService.isAdmin()) {
            return studentRepository.findAll(pageable);
        } else if (permissionService.isCounselor()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                return studentRepository.findByCounselor(currentUser, pageable);
            }
        } else if (permissionService.isWriter()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                return studentRepository.findByWriter(currentUser, pageable);
            }
        }
        return Page.empty();
    }

    /**
     * 分页搜索学生（根据权限过滤）
     */
    public Page<Student> searchStudentsByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (permissionService.isAdmin()) {
            return studentRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (permissionService.isCounselor()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                return studentRepository.findByCounselorAndNameContaining(currentUser, name, pageable);
            }
        } else if (permissionService.isWriter()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                return studentRepository.findByWriterAndNameContaining(currentUser, name, pageable);
            }
        }
        return Page.empty();
    }

    /**
     * 根据咨询顾问获取学生
     */
    public List<Student> getStudentsByCounselor(User counselor) {
        return studentRepository.findByCounselor(counselor);
    }

    /**
     * 根据文案获取学生
     */
    public List<Student> getStudentsByWriter(User writer) {
        return studentRepository.findByWriter(writer);
    }

    /**
     * 根据姓名搜索学生
     */
    public List<Student> searchStudentsByName(String name) {
        if (permissionService.isAdmin()) {
            return studentRepository.findByNameContainingIgnoreCase(name);
        } else if (permissionService.isCounselor()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                return studentRepository.findByCounselorAndNameContaining(currentUser, name);
            }
        } else if (permissionService.isWriter()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                return studentRepository.findByWriterAndNameContaining(currentUser, name);
            }
        }
        return List.of();
    }

    /**
     * 更新学生信息
     */
    public Student updateStudent(Long id, CreateStudentRequest request) {
        Student existingStudent = getStudentById(id);
        
        // 检查权限：咨询顾问只能修改自己的学生
        if (!permissionService.isAdmin() && permissionService.isCounselor()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser == null || existingStudent.getCounselor() == null || 
                !currentUser.getId().equals(existingStudent.getCounselor().getId())) {
                throw new RuntimeException("没有权限修改此学生信息");
            }
        }
        
        // 更新字段
        existingStudent.setName(request.getName());
        existingStudent.setStudentSource(request.getStudentSource());
        existingStudent.setStatus(request.getStatus());
        existingStudent.setGender(request.getGender());
        existingStudent.setBirthDate(request.getBirthDate());
        existingStudent.setNationality(request.getNationality());
        existingStudent.setIdCard(request.getIdCard());
        existingStudent.setAddress(request.getAddress());
        existingStudent.setPhone(request.getPhone());
        existingStudent.setEmail(request.getEmail());
        existingStudent.setWechat(request.getWechat());
        existingStudent.setCurrentSchool(request.getCurrentSchool());
        existingStudent.setEnrollmentDate(request.getEnrollmentDate());
        existingStudent.setChannelSource(request.getChannelSource());
        existingStudent.setContractDate(request.getContractDate());
        existingStudent.setContractAmount(request.getContractAmount());
        existingStudent.setMajor(request.getMajor());
        existingStudent.setGpa(request.getGpa());
        existingStudent.setToeflScore(request.getToeflScore());
        existingStudent.setIeltsScore(request.getIeltsScore());
        existingStudent.setGreScore(request.getGreScore());
        existingStudent.setGmatScore(request.getGmatScore());
        existingStudent.setAwards(request.getAwards());
        existingStudent.setExperiences(request.getExperiences());
        existingStudent.setNotes(request.getNotes());
        
        // 更新咨询顾问和文案
        // 只有管理员可以修改咨询顾问
        if (permissionService.isAdmin() && request.getCounselorId() != null) {
            existingStudent.setCounselor(userRepository.findById(request.getCounselorId()).orElse(null));
        }
        // 咨询顾问不能修改学生的咨询顾问字段，但可以设置文案
        else if (permissionService.isCounselor() && !permissionService.isAdmin()) {
            // 咨询顾问修改学生时，保持原有的咨询顾问信息不变
            // 不处理咨询顾问字段，确保学生始终属于创建该学生的顾问
        }
        
        // 管理员和咨询顾问都可以设置文案
        if ((permissionService.isAdmin() || permissionService.isCounselor()) && request.getWriterId() != null) {
            existingStudent.setWriter(userRepository.findById(request.getWriterId()).orElse(null));
        }
        
        return studentRepository.save(existingStudent);
    }

    /**
     * 删除学生
     */
    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        
        // 只有管理员可以删除学生
        if (!permissionService.isAdmin()) {
            throw new RuntimeException("没有权限删除学生信息，只有管理员可以删除学生");
        }
        
        studentRepository.deleteById(id);
    }

    /**
     * 为咨询顾问分配学生
     */
    public Student assignStudentToCounselor(Long studentId, Long counselorId) {
        if (!permissionService.isAdmin()) {
            throw new RuntimeException("只有管理员可以分配学生");
        }
        
        Student student = getStudentById(studentId);
        User counselor = userRepository.findById(counselorId).orElse(null);
        student.setCounselor(counselor);
        return studentRepository.save(student);
    }

    /**
     * 为文案分配学生
     */
    public Student assignStudentToWriter(Long studentId, Long writerId) {
        if (!permissionService.isAdmin()) {
            throw new RuntimeException("只有管理员可以分配学生");
        }
        
        Student student = getStudentById(studentId);
        User writer = userRepository.findById(writerId).orElse(null);
        student.setWriter(writer);
        return studentRepository.save(student);
    }
}
