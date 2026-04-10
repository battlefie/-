package com.studyabroad.service;

import com.studyabroad.dto.CreateStudentRequest;
import com.studyabroad.entity.Student;
import com.studyabroad.entity.Application;
import com.studyabroad.entity.User;
import com.studyabroad.entity.FamilyInfo;
import com.studyabroad.dto.ApplicationSummary;
import com.studyabroad.repository.StudentRepository;
import com.studyabroad.repository.UserRepository;
import com.studyabroad.repository.ApplicationRepository;
import com.studyabroad.repository.FamilyInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;

/**
 * 学生服务类
 */
@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final FamilyInfoRepository familyInfoRepository;
    private final PermissionService permissionService;

    public StudentService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          ApplicationRepository applicationRepository,
                          FamilyInfoRepository familyInfoRepository,
                          PermissionService permissionService) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.familyInfoRepository = familyInfoRepository;
        this.permissionService = permissionService;
    }

    /**
     * 创建学生
     */
    public Student createStudent(CreateStudentRequest request) {
        Student student = new Student();
        student.setName(request.getName());
        student.setStatus(request.getStatus());
        student.setGender(request.getGender());
        student.setBirthDate(request.getBirthDate());
        student.setIdCard(request.getIdCard());
        student.setAddress(request.getAddress());
        student.setContactInfo(request.getContactInfo());
        student.setCurrentSchool(request.getCurrentSchool());
        student.setChannelSource(request.getChannelSource());
        student.setIntendedCountry(request.getIntendedCountry());
        student.setEnrolledCountry(request.getEnrolledCountry());
        student.setEnrolledSchool(request.getEnrolledSchool());
        student.setContractDate(request.getContractDate());
        student.setContractAmount(request.getContractAmount());
        student.setMajor(request.getMajor());
        student.setGpa(request.getGpa());
        student.setLanguageScores(request.getLanguageScores());
        student.setAwards(request.getAwards());
        student.setExperiences(request.getExperiences());
        student.setNotes(request.getNotes());
        
        User currentUser = permissionService.getCurrentUser();
        boolean isAdmin = permissionService.isAdmin();
        boolean isCounselor = permissionService.isCounselor();
        boolean isWriter = permissionService.isWriter();

        // 设置咨询顾问
        if (isAdmin && request.getCounselorId() != null) {
            student.setCounselor(userRepository.findById(request.getCounselorId()).orElse(null));
        } else if (isCounselor && !isAdmin && currentUser != null) {
            student.setCounselor(currentUser);
        }

        // 设置文案
        // 管理员和顾问都可以在创建学生时分配文案
        if ((isAdmin || isCounselor) && request.getWriterId() != null) {
            student.setWriter(userRepository.findById(request.getWriterId()).orElse(null));
        } else if (isWriter && !isAdmin && currentUser != null) {
            student.setWriter(currentUser);
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
     * 根据联系方式获取学生（联系方式中包含邮箱）
     */
    public Student getStudentByContactInfo(String contactInfo) {
        // 使用高级搜索查询联系方式
        return studentRepository.findAll().stream()
                .filter(s -> s.getContactInfo() != null && s.getContactInfo().contains(contactInfo))
                .findFirst()
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
     * 获取学生及其申请概览
     */
    public List<Student> getStudentsWithApplications(String name) {
        List<Student> baseList;
        if (name != null && !name.trim().isEmpty()) {
            baseList = searchStudentsByName(name);
        } else {
            baseList = getAllStudents();
        }

        if (baseList.isEmpty()) {
            return baseList;
        }

        List<Long> studentIds = baseList.stream()
                .map(Student::getId)
                .collect(Collectors.toList());

        List<Application> applications = applicationRepository.findByStudentIdIn(studentIds);
        Map<Long, List<ApplicationSummary>> appsGrouped = applications.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getStudent().getId(),
                        Collectors.mapping(ApplicationSummary::fromEntity, Collectors.toList())
                ));

        List<FamilyInfo> familyInfos = familyInfoRepository.findByStudentIdIn(studentIds);
        Map<Long, FamilyInfo> familyInfoMap = familyInfos.stream()
                .filter(fi -> fi.getStudent() != null && fi.getStudent().getId() != null)
                .collect(Collectors.toMap(
                        fi -> fi.getStudent().getId(),
                        fi -> fi
                ));

        baseList.forEach(student -> {
            student.setApplications(
                    appsGrouped.getOrDefault(student.getId(), List.of())
            );
            student.setFamilyInfo(
                    familyInfoMap.getOrDefault(student.getId(), null)
            );
        });

        return baseList;
    }

    /**
     * 分页获取学生及其申请概览
     */
    public Page<Student> getStudentsWithApplications(String name, int page, int size) {
        Page<Student> studentPage;
        if (name != null && !name.trim().isEmpty()) {
            studentPage = searchStudentsByName(name, page, size);
        } else {
            studentPage = getStudents(page, size);
        }

        if (studentPage.isEmpty()) {
            return studentPage;
        }

        List<Long> studentIds = studentPage.getContent().stream()
                .map(Student::getId)
                .collect(Collectors.toList());

        List<Application> applications = applicationRepository.findByStudentIdIn(studentIds);
        Map<Long, List<ApplicationSummary>> appsGrouped = applications.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getStudent().getId(),
                        Collectors.mapping(ApplicationSummary::fromEntity, Collectors.toList())
                ));

        List<FamilyInfo> familyInfos = familyInfoRepository.findByStudentIdIn(studentIds);
        Map<Long, FamilyInfo> familyInfoMap = familyInfos.stream()
                .filter(fi -> fi.getStudent() != null && fi.getStudent().getId() != null)
                .collect(Collectors.toMap(
                        fi -> fi.getStudent().getId(),
                        fi -> fi
                ));

        studentPage.getContent().forEach(student -> {
            student.setApplications(
                    appsGrouped.getOrDefault(student.getId(), List.of())
            );
            student.setFamilyInfo(
                    familyInfoMap.getOrDefault(student.getId(), null)
            );
        });

        return studentPage;
    }

    /**
     * 分页获取学生（根据权限过滤）
     * 按签约时间降序排序，新签约客户显示在最前面
     * 如果签约时间相同，按创建时间降序排序，确保新转换的客户显示在最前面
     */
    public Page<Student> getStudents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.DESC, "contractDate")
                .and(Sort.by(Sort.Direction.DESC, "createTime"))
                .and(Sort.by(Sort.Direction.DESC, "id")));
        if (permissionService.isAdmin()) {
            return studentRepository.findAll(pageable);
        } else if (permissionService.isCounselor()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                return studentRepository.findByCounselorOrderByContractDateDesc(currentUser, pageable);
            }
        } else if (permissionService.isWriter()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                return studentRepository.findByWriterOrderByContractDateDesc(currentUser, pageable);
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
        User currentUser = permissionService.getCurrentUser();
        boolean isAdmin = permissionService.isAdmin();
        boolean isCounselor = permissionService.isCounselor();
        boolean isWriter = permissionService.isWriter();
        
        if (!isAdmin) {
            if (isCounselor) {
            if (currentUser == null || existingStudent.getCounselor() == null || 
                !currentUser.getId().equals(existingStudent.getCounselor().getId())) {
                throw new RuntimeException("没有权限修改此学生信息");
            }
            } else if (isWriter) {
                if (currentUser == null || existingStudent.getWriter() == null ||
                        !currentUser.getId().equals(existingStudent.getWriter().getId())) {
                    throw new RuntimeException("没有权限修改此学生信息");
                }
            } else {
                throw new RuntimeException("没有权限修改学生信息");
            }
        }
        
        // 更新字段（必填字段始终更新，其他字段允许null来清空）
        existingStudent.setName(request.getName());
        existingStudent.setStatus(request.getStatus());
        existingStudent.setGender(request.getGender());
        existingStudent.setBirthDate(request.getBirthDate());
        // 更新字段（允许null来清空字段）
        existingStudent.setIdCard(request.getIdCard());
        existingStudent.setAddress(request.getAddress());
        existingStudent.setContactInfo(request.getContactInfo());
        existingStudent.setCurrentSchool(request.getCurrentSchool());
        existingStudent.setChannelSource(request.getChannelSource());
        existingStudent.setIntendedCountry(request.getIntendedCountry());
        existingStudent.setEnrolledCountry(request.getEnrolledCountry());
        existingStudent.setEnrolledSchool(request.getEnrolledSchool());
        existingStudent.setContractDate(request.getContractDate());
        existingStudent.setContractAmount(request.getContractAmount());
        existingStudent.setMajor(request.getMajor());
        existingStudent.setGpa(request.getGpa());
        existingStudent.setLanguageScores(request.getLanguageScores());
        existingStudent.setAwards(request.getAwards());
        existingStudent.setExperiences(request.getExperiences());
        existingStudent.setNotes(request.getNotes());
        
        // 更新咨询顾问和文案
        // 文案不能修改顾问和文案字段
        if (isWriter && !isAdmin) {
            // 文案编辑时，不允许修改顾问和文案字段，保持原有值
            // 不执行任何更新操作
        } else {
            // 管理员可以修改咨询顾问和文案
            if (isAdmin) {
                // 管理员可以修改顾问
                if (request.getCounselorId() != null) {
                    existingStudent.setCounselor(userRepository.findById(request.getCounselorId()).orElse(null));
                }
                // 如果counselorId为null，保持原有值不变（不更新）
                
                // 管理员可以修改文案
                if (request.getWriterId() != null) {
                    existingStudent.setWriter(userRepository.findById(request.getWriterId()).orElse(null));
                }
                // 如果writerId为null，保持原有值不变（不更新）
            } else if (isCounselor && !isAdmin && currentUser != null) {
                // 顾问编辑时，顾问字段保持不变（前端已禁用），但可以修改文案
                // 如果请求中有文案ID，允许顾问修改文案
                if (request.getWriterId() != null) {
                    existingStudent.setWriter(userRepository.findById(request.getWriterId()).orElse(null));
                }
            }
        }
        
        return studentRepository.save(existingStudent);
    }

    /**
     * 删除学生
     */
    public void deleteStudent(Long id) {
        // 检查学生是否存在
        getStudentById(id);
        
        // 只有超级管理员可以删除学生
        if (!permissionService.isSuperAdmin()) {
            throw new RuntimeException("没有权限删除学生信息，只有超级管理员可以删除学生");
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

    /**
     * 高级搜索学生
     */
    public Page<Student> advancedSearch(
            String name, String idCard, String contact, String gender,
            String currentSchool, String major, String intendedCountry, String status,
            String fatherName, String fatherContact, String fatherWorkInfo,
            String motherName, String motherContact, String motherWorkInfo,
            LocalDate contractDateStart, LocalDate contractDateEnd,
            int page, int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        
        // 根据权限过滤结果
        User currentUser = permissionService.getCurrentUser();
        boolean isAdmin = permissionService.isAdmin();
        
        Page<Student> result;
        
        if (isAdmin) {
            // 管理员可以搜索所有学生
            result = studentRepository.advancedSearch(
                    name, idCard, contact, gender,
                    currentSchool, major, intendedCountry, status,
                    fatherName, fatherContact, fatherWorkInfo,
                    motherName, motherContact, motherWorkInfo,
                    contractDateStart, contractDateEnd,
                    pageable);
        } else if (permissionService.isCounselor() && currentUser != null) {
            // 咨询顾问只能搜索自己的学生
            // 先搜索所有匹配的学生，然后过滤
            Page<Student> allResults = studentRepository.advancedSearch(
                    name, idCard, contact, gender,
                    currentSchool, major, intendedCountry, status,
                    fatherName, fatherContact, fatherWorkInfo,
                    motherName, motherContact, motherWorkInfo,
                    contractDateStart, contractDateEnd,
                    PageRequest.of(0, 10000)); // 获取所有结果以便过滤
            
            List<Student> filtered = allResults.getContent().stream()
                    .filter(s -> s.getCounselor() != null && 
                            s.getCounselor().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
            
            // 手动分页
            int start = page * size;
            int end = Math.min(start + size, filtered.size());
            List<Student> pageContent = start < filtered.size() 
                    ? filtered.subList(start, end) 
                    : List.of();
            
            result = new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, filtered.size());
        } else if (permissionService.isWriter() && currentUser != null) {
            // 文案只能搜索自己的学生
            Page<Student> allResults = studentRepository.advancedSearch(
                    name, idCard, contact, gender,
                    currentSchool, major, intendedCountry, status,
                    fatherName, fatherContact, fatherWorkInfo,
                    motherName, motherContact, motherWorkInfo,
                    contractDateStart, contractDateEnd,
                    PageRequest.of(0, 10000));
            
            List<Student> filtered = allResults.getContent().stream()
                    .filter(s -> s.getWriter() != null && 
                            s.getWriter().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
            
            int start = page * size;
            int end = Math.min(start + size, filtered.size());
            List<Student> pageContent = start < filtered.size() 
                    ? filtered.subList(start, end) 
                    : List.of();
            
            result = new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, filtered.size());
        } else {
            throw new RuntimeException("没有权限进行高级搜索");
        }
        
        // 加载申请和家庭信息
        if (!result.isEmpty()) {
            List<Long> studentIds = result.getContent().stream()
                    .map(Student::getId)
                    .collect(Collectors.toList());
            
            // 加载申请信息
            List<Application> applications = applicationRepository.findByStudentIdIn(studentIds);
            Map<Long, List<ApplicationSummary>> applicationMap = applications.stream()
                    .collect(Collectors.groupingBy(
                            app -> app.getStudent().getId(),
                            Collectors.mapping(ApplicationSummary::fromEntity, Collectors.toList())));
            
            // 加载家庭信息
            List<FamilyInfo> familyInfos = familyInfoRepository.findByStudentIdIn(studentIds);
            Map<Long, FamilyInfo> familyInfoMap = familyInfos.stream()
                    .filter(fi -> fi.getStudent() != null && fi.getStudent().getId() != null)
                    .collect(Collectors.toMap(fi -> fi.getStudent().getId(), fi -> fi));
            
            // 设置申请和家庭信息
            result.getContent().forEach(student -> {
                student.setApplications(applicationMap.getOrDefault(student.getId(), List.of()));
                student.setFamilyInfo(familyInfoMap.get(student.getId()));
            });
        }
        
        return result;
    }
}
