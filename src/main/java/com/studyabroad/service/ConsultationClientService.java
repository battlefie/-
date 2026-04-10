package com.studyabroad.service;

import com.studyabroad.entity.ConsultationClient;
import com.studyabroad.entity.Student;
import com.studyabroad.entity.User;
import com.studyabroad.repository.ConsultationClientRepository;
import com.studyabroad.repository.StudentRepository;
import com.studyabroad.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 咨询客户服务类
 */
@Service
public class ConsultationClientService {

    private final ConsultationClientRepository consultationClientRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    public ConsultationClientService(ConsultationClientRepository consultationClientRepository,
                                     StudentRepository studentRepository,
                                     UserRepository userRepository,
                                     PermissionService permissionService) {
        this.consultationClientRepository = consultationClientRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.permissionService = permissionService;
    }

    /**
     * 创建咨询客户（权限控制：顾问和文案只能创建分配给自己的客户）
     */
    @Transactional
    public ConsultationClient createConsultationClient(ConsultationClient client) {
        // 如果是顾问或文案，确保只能创建分配给自己的客户
        if (!permissionService.isAdmin()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                if (permissionService.isCounselor()) {
                    // 顾问只能创建分配给自己的客户
                    if (client.getCounselorId() == null || !client.getCounselorId().equals(currentUser.getId())) {
                        throw new RuntimeException("您只能创建分配给自己的咨询客户");
                    }
                } else if (permissionService.isWriter()) {
                    // 文案只能创建分配给自己的客户
                    if (client.getWriterId() == null || !client.getWriterId().equals(currentUser.getId())) {
                        throw new RuntimeException("您只能创建分配给自己的咨询客户");
                    }
                }
            }
        }
        return consultationClientRepository.save(client);
    }

    /**
     * 获取所有咨询客户（根据权限过滤）
     */
    public List<ConsultationClient> getAllConsultationClients() {
        User currentUser = permissionService.getCurrentUser();
        if (permissionService.isAdmin()) {
            return consultationClientRepository.findAll();
        } else if (permissionService.isCounselor()) {
            if (currentUser != null) {
                List<ConsultationClient> clients = consultationClientRepository.findByCounselorId(currentUser.getId());
                return clients;
            }
        } else if (permissionService.isWriter()) {
            if (currentUser != null) {
                List<ConsultationClient> clients = consultationClientRepository.findByWriterId(currentUser.getId());
                return clients;
            }
        }
        return List.of();
    }

    /**
     * 分页获取咨询客户（根据权限过滤）
     * 按咨询时间降序排序，咨询时间最晚的客户显示在最前面
     */
    public Page<ConsultationClient> getConsultationClients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User currentUser = permissionService.getCurrentUser();
        
        if (permissionService.isAdmin()) {
            return consultationClientRepository.findAllByOrderByCreateTimeDesc(pageable);
        } else if (permissionService.isCounselor()) {
            if (currentUser != null) {
                return consultationClientRepository.findByCounselorIdOrderByCreateTimeDesc(currentUser.getId(), pageable);
            }
        } else if (permissionService.isWriter()) {
            if (currentUser != null) {
                return consultationClientRepository.findByWriterIdOrderByCreateTimeDesc(currentUser.getId(), pageable);
            }
        }
        return Page.empty(pageable);
    }

    /**
     * 根据ID获取咨询客户（需要权限验证）
     */
    public ConsultationClient getConsultationClientById(Long id) {
        ConsultationClient client = consultationClientRepository.findByIdWithUsers(id)
                .orElseThrow(() -> new RuntimeException("咨询客户不存在"));
        
        // 权限验证：顾问和文案只能查看自己的客户
        if (!permissionService.isAdmin()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                if (permissionService.isCounselor()) {
                    // 顾问只能查看分配给自己的客户
                    if (client.getCounselor() == null || !client.getCounselor().getId().equals(currentUser.getId())) {
                        throw new RuntimeException("您只能查看自己的咨询客户");
                    }
                } else if (permissionService.isWriter()) {
                    // 文案只能查看分配给自己的客户
                    if (client.getWriter() == null || !client.getWriter().getId().equals(currentUser.getId())) {
                        throw new RuntimeException("您只能查看自己的咨询客户");
                    }
                }
            }
        }
        
        return client;
    }

    /**
     * 根据状态获取咨询客户（根据权限过滤）
     */
    public List<ConsultationClient> getConsultationClientsByStatus(ConsultationClient.ClientStatus status) {
        List<ConsultationClient> clients;
        if (permissionService.isAdmin()) {
            clients = consultationClientRepository.findByStatus(status);
        } else if (permissionService.isCounselor()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                clients = consultationClientRepository.findByCounselorId(currentUser.getId());
                // 过滤状态
                return clients.stream()
                        .filter(c -> c.getStatus() == status)
                        .collect(java.util.stream.Collectors.toList());
            } else {
                return List.of();
            }
        } else if (permissionService.isWriter()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                clients = consultationClientRepository.findByWriterId(currentUser.getId());
                // 过滤状态
                return clients.stream()
                        .filter(c -> c.getStatus() == status)
                        .collect(java.util.stream.Collectors.toList());
            } else {
                return List.of();
            }
        } else {
            return List.of();
        }
        return clients;
    }

    /**
     * 更新咨询客户
     * 当状态变为"签约客户"时，自动将客户信息复制到签约客户系统（学生表），咨询客户记录保留
     */
    @Transactional
    public ConsultationClient updateConsultationClient(Long id, ConsultationClient clientDetails) {
        ConsultationClient client = getConsultationClientById(id); // 这里已经包含了权限验证
        
        // 如果是顾问或文案，确保不能修改分配给其他人的客户
        if (!permissionService.isAdmin()) {
            User currentUser = permissionService.getCurrentUser();
            if (currentUser != null) {
                if (permissionService.isCounselor()) {
                    // 顾问只能更新分配给自己的客户
                    if (client.getCounselor() == null || !client.getCounselor().getId().equals(currentUser.getId())) {
                        throw new RuntimeException("您只能更新自己的咨询客户");
                    }
                } else if (permissionService.isWriter()) {
                    // 文案只能更新分配给自己的客户
                    if (client.getWriter() == null || !client.getWriter().getId().equals(currentUser.getId())) {
                        throw new RuntimeException("您只能更新自己的咨询客户");
                    }
                }
            }
        }
        
        // 记录原始状态
        ConsultationClient.ClientStatus oldStatus = client.getStatus();
        
        // 更新字段
        if (clientDetails.getName() != null) client.setName(clientDetails.getName());
        if (clientDetails.getContactInfo() != null) client.setContactInfo(clientDetails.getContactInfo());
        
        // 重要：先更新状态，因为后续的状态比较需要用到新状态
        ConsultationClient.ClientStatus newStatus = null;
        if (clientDetails.getStatus() != null) {
            newStatus = clientDetails.getStatus();
            client.setStatus(newStatus);
            System.out.println("更新状态: " + oldStatus + " -> " + newStatus);
        } else {
            newStatus = client.getStatus(); // 如果没有提供新状态，使用当前状态
            System.out.println("未提供新状态，保持当前状态: " + newStatus);
        }
        
        if (clientDetails.getConsultationDate() != null) client.setConsultationDate(clientDetails.getConsultationDate());
        if (clientDetails.getGender() != null) client.setGender(clientDetails.getGender());
        if (clientDetails.getBirthDate() != null) client.setBirthDate(clientDetails.getBirthDate());
        if (clientDetails.getIdCard() != null) client.setIdCard(clientDetails.getIdCard());
        if (clientDetails.getAddress() != null) client.setAddress(clientDetails.getAddress());
        if (clientDetails.getChannelSource() != null) client.setChannelSource(clientDetails.getChannelSource());
        if (clientDetails.getIntendedCountry() != null) client.setIntendedCountry(clientDetails.getIntendedCountry());
        if (clientDetails.getCurrentSchool() != null) client.setCurrentSchool(clientDetails.getCurrentSchool());
        if (clientDetails.getEnrollmentDate() != null) client.setEnrollmentDate(clientDetails.getEnrollmentDate());
        if (clientDetails.getMajor() != null) client.setMajor(clientDetails.getMajor());
        if (clientDetails.getGpa() != null) client.setGpa(clientDetails.getGpa());
        if (clientDetails.getLanguageScores() != null) client.setLanguageScores(clientDetails.getLanguageScores());
        if (clientDetails.getAwards() != null) client.setAwards(clientDetails.getAwards());
        if (clientDetails.getExperiences() != null) client.setExperiences(clientDetails.getExperiences());
        if (clientDetails.getNotes() != null) client.setNotes(clientDetails.getNotes());
        if (clientDetails.getFollowUpStatus() != null) client.setFollowUpStatus(clientDetails.getFollowUpStatus());
        if (clientDetails.getCounselorId() != null) client.setCounselorId(clientDetails.getCounselorId());
        if (clientDetails.getWriterId() != null) client.setWriterId(clientDetails.getWriterId());
        
        // 如果状态从非"签约客户"变为"签约客户"，复制信息到签约客户系统（学生表）
        System.out.println("状态转换检查: 旧状态=" + oldStatus + ", 新状态=" + newStatus);
        
        if (oldStatus != ConsultationClient.ClientStatus.签约客户 && 
            newStatus == ConsultationClient.ClientStatus.签约客户) {
            System.out.println("开始复制咨询客户信息到签约客户系统: " + client.getName());
            try {
                // 复制信息到学生表
                Student student = convertToStudent(client);
                Student savedStudent = studentRepository.save(student);
                System.out.println("学生创建成功，ID: " + savedStudent.getId());
                
                // 保留咨询客户记录，不删除
                // 咨询客户记录会保存，状态为"签约客户"
                System.out.println("咨询客户记录已保留，ID: " + id);
            } catch (Exception e) {
                System.err.println("复制咨询客户信息到签约客户系统时发生错误: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("复制咨询客户信息到签约客户系统失败: " + e.getMessage(), e);
            }
        }
        
        // 保存咨询客户记录（无论是否转换为签约客户，都保留咨询客户记录）
        return consultationClientRepository.save(client);
    }

    /**
     * 将咨询客户信息复制到学生表（签约客户系统）
     */
    private Student convertToStudent(ConsultationClient client) {
        Student student = new Student();
        
        // 基本信息
        student.setName(client.getName());
        student.setStatus(Student.StudentStatus.ACTIVE); // 默认设置为"在途"
        student.setGender(client.getGender());
        student.setBirthDate(client.getBirthDate());
        student.setIdCard(client.getIdCard());
        student.setAddress(client.getAddress());
        // 直接使用联系方式
        student.setContactInfo(client.getContactInfo());
        
        // 学校信息
        student.setCurrentSchool(client.getCurrentSchool());
        student.setChannelSource(client.getChannelSource());
        student.setIntendedCountry(client.getIntendedCountry());
        student.setEnrolledCountry(null); // 咨询客户没有入读国家字段
        student.setEnrolledSchool(null); // 咨询客户没有入读院校字段
        
        // 签约信息：无论是否有咨询日期，签约时间统一为当前日期
        student.setContractDate(java.time.LocalDate.now());
        student.setContractAmount(null); // 咨询客户没有签约金额字段
        
        // 学术信息
        student.setMajor(client.getMajor());
        student.setGpa(client.getGpa());
        student.setLanguageScores(client.getLanguageScores());
        student.setAwards(client.getAwards());
        student.setExperiences(client.getExperiences());
        
        // 备注（合并咨询备注和回访情况）
        String notes = client.getNotes();
        if (client.getFollowUpStatus() != null && !client.getFollowUpStatus().isEmpty()) {
            if (notes != null && !notes.isEmpty()) {
                notes += "\n\n回访情况：" + client.getFollowUpStatus();
            } else {
                notes = "回访情况：" + client.getFollowUpStatus();
            }
        }
        student.setNotes(notes);
        
        // 设置咨询顾问和文案
        if (client.getCounselor() != null) {
            student.setCounselor(client.getCounselor());
        } else if (client.getCounselorId() != null) {
            userRepository.findById(client.getCounselorId()).ifPresent(student::setCounselor);
        }
        if (client.getWriter() != null) {
            student.setWriter(client.getWriter());
        } else if (client.getWriterId() != null) {
            userRepository.findById(client.getWriterId()).ifPresent(student::setWriter);
        }
        
        return student;
    }

    /**
     * 删除咨询客户（需要权限验证）
     * 只有超级管理员可以删除咨询客户
     */
    @Transactional
    public void deleteConsultationClient(Long id) {
        // 权限检查：只有超级管理员可以删除咨询客户
        if (!permissionService.isSuperAdmin()) {
            throw new RuntimeException("只有超级管理员可以删除咨询客户");
                    }
        
        // 验证咨询客户是否存在
        consultationClientRepository.findByIdWithUsers(id)
                .orElseThrow(() -> new RuntimeException("咨询客户不存在"));
        
        consultationClientRepository.deleteById(id);
    }

    /**
     * 高级搜索咨询客户
     * 按咨询时间降序排序，咨询时间最晚的客户显示在最前面
     */
    public Page<ConsultationClient> advancedSearch(
            String name, String idCard, String contact, String gender,
            String currentSchool, String major, String intendedCountry,
            String channelSource, ConsultationClient.ClientStatus status,
            java.time.LocalDate consultationDateStart, java.time.LocalDate consultationDateEnd,
            int page, int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        User currentUser = permissionService.getCurrentUser();
        
        Page<ConsultationClient> result;
        
        if (permissionService.isAdmin()) {
            // 管理员可以搜索所有客户
            result = consultationClientRepository.advancedSearch(
                    name, idCard, contact, gender,
                    currentSchool, major, intendedCountry, channelSource, status,
                    consultationDateStart, consultationDateEnd,
                    pageable);
        } else if (permissionService.isCounselor() && currentUser != null) {
            // 咨询顾问只能搜索自己的客户
            // 先搜索所有匹配的客户，然后过滤
            Page<ConsultationClient> allResults = consultationClientRepository.advancedSearch(
                    name, idCard, contact, gender,
                    currentSchool, major, intendedCountry, channelSource, status,
                    consultationDateStart, consultationDateEnd,
                    PageRequest.of(0, 10000)); // 获取所有结果以便过滤
            
            List<ConsultationClient> filtered = allResults.getContent().stream()
                    .filter(c -> c.getCounselor() != null && 
                            c.getCounselor().getId().equals(currentUser.getId()))
                    .collect(java.util.stream.Collectors.toList());
            
            // 手动分页
            int start = page * size;
            int end = Math.min(start + size, filtered.size());
            List<ConsultationClient> pageContent = start < filtered.size() 
                    ? filtered.subList(start, end) 
                    : List.of();
            
            result = new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, filtered.size());
        } else if (permissionService.isWriter() && currentUser != null) {
            // 文案只能搜索自己的客户
            Page<ConsultationClient> allResults = consultationClientRepository.advancedSearch(
                    name, idCard, contact, gender,
                    currentSchool, major, intendedCountry, channelSource, status,
                    consultationDateStart, consultationDateEnd,
                    PageRequest.of(0, 10000)); // 获取所有结果以便过滤
            
            List<ConsultationClient> filtered = allResults.getContent().stream()
                    .filter(c -> c.getWriter() != null && 
                            c.getWriter().getId().equals(currentUser.getId()))
                    .collect(java.util.stream.Collectors.toList());
            
            // 手动分页
            int start = page * size;
            int end = Math.min(start + size, filtered.size());
            List<ConsultationClient> pageContent = start < filtered.size() 
                    ? filtered.subList(start, end) 
                    : List.of();
            
            result = new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, filtered.size());
        } else {
            // 无权限，返回空结果
            result = new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0);
        }
        
        return result;
    }
}





