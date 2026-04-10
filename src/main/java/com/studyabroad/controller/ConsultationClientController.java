package com.studyabroad.controller;

import com.studyabroad.dto.ApiResponse;
import com.studyabroad.dto.CreateConsultationClientRequest;
import com.studyabroad.entity.ConsultationClient;
import com.studyabroad.service.ConsultationClientService;
import com.studyabroad.service.PermissionService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 咨询客户控制器
 */
@RestController
@RequestMapping("/api/consultation-clients")
public class ConsultationClientController {

    private final ConsultationClientService consultationClientService;
    private final PermissionService permissionService;

    public ConsultationClientController(ConsultationClientService consultationClientService, PermissionService permissionService) {
        this.consultationClientService = consultationClientService;
        this.permissionService = permissionService;
    }

    @PostMapping
    public ApiResponse<ConsultationClient> createConsultationClient(@RequestBody CreateConsultationClientRequest request) {
        try {
            // 权限控制：只有管理员、咨询顾问和文案可以创建
            if (!permissionService.isAdmin() && !permissionService.isCounselor() && !permissionService.isWriter()) {
                return ApiResponse.error("您没有权限创建咨询客户");
            }
            
            ConsultationClient client = convertToEntity(request);
            
            // 如果是顾问或文案，自动设置为当前用户
            if (!permissionService.isAdmin()) {
                if (permissionService.isCounselor()) {
                    // 顾问创建时，自动设置为当前顾问
                    client.setCounselorId(permissionService.getCurrentUser().getId());
                } else if (permissionService.isWriter()) {
                    // 文案创建时，自动设置为当前文案
                    client.setWriterId(permissionService.getCurrentUser().getId());
                }
            }
            
            ConsultationClient createdClient = consultationClientService.createConsultationClient(client);
            return ApiResponse.success("咨询客户创建成功", createdClient);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 将DTO转换为实体
     */
    private ConsultationClient convertToEntity(CreateConsultationClientRequest request) {
        ConsultationClient client = new ConsultationClient();
        client.setName(request.getName());
        
        // 确保状态正确设置
        if (request.getStatus() != null) {
            client.setStatus(request.getStatus());
            System.out.println("设置状态: " + request.getStatus());
        } else {
            System.out.println("警告: 请求中的状态为null");
        }
        
        client.setGender(request.getGender());
        client.setBirthDate(request.getBirthDate());
        client.setIdCard(request.getIdCard());
        client.setAddress(request.getAddress());
        client.setContactInfo(request.getContactInfo());
        client.setCurrentSchool(request.getCurrentSchool());
        client.setEnrollmentDate(request.getEnrollmentDate());
        client.setChannelSource(request.getChannelSource());
        client.setIntendedCountry(request.getIntendedCountry());
        client.setMajor(request.getMajor());
        client.setGpa(request.getGpa());
        client.setLanguageScores(request.getLanguageScores());
        client.setAwards(request.getAwards());
        client.setExperiences(request.getExperiences());
        client.setNotes(request.getNotes());
        client.setConsultationDate(request.getConsultationDate());
        client.setFollowUpStatus(request.getFollowUpStatus());
        client.setCounselorId(request.getCounselorId());
        client.setWriterId(request.getWriterId());
        return client;
    }

    @GetMapping("/{id}")
    public ApiResponse<ConsultationClient> getConsultationClientById(@PathVariable Long id) {
        try {
            // 权限控制：管理员、咨询顾问和文案可以查看
            if (!permissionService.isAdmin() && !permissionService.isCounselor() && !permissionService.isWriter()) {
                return ApiResponse.error("您没有权限访问咨询客户信息");
            }
            
            ConsultationClient client = consultationClientService.getConsultationClientById(id);
            return ApiResponse.success(client);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<?> getConsultationClients(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) ConsultationClient.ClientStatus status) {
        try {
            // 权限控制：管理员、咨询顾问和文案可以查看
            if (!permissionService.isAdmin() && !permissionService.isCounselor() && !permissionService.isWriter()) {
                return ApiResponse.error("您没有权限访问咨询客户信息");
            }
            
            // 如果提供了分页参数，返回分页数据（服务层会根据权限自动过滤）
            if (page != null && size != null) {
                Page<ConsultationClient> clientsPage = consultationClientService.getConsultationClients(page, size);
                return ApiResponse.success(clientsPage);
            }
            
            // 如果提供了状态筛选，返回按状态筛选的数据（服务层会根据权限自动过滤）
            if (status != null) {
                List<ConsultationClient> clients = consultationClientService.getConsultationClientsByStatus(status);
                return ApiResponse.success(clients);
            }
            
            // 否则返回所有数据（服务层会根据权限自动过滤）
            List<ConsultationClient> clients = consultationClientService.getAllConsultationClients();
            return ApiResponse.success(clients);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateConsultationClient(@PathVariable Long id, @RequestBody CreateConsultationClientRequest request) {
        try {
            // 权限控制：管理员、咨询顾问和文案可以更新
            if (!permissionService.isAdmin() && !permissionService.isCounselor() && !permissionService.isWriter()) {
                return ApiResponse.error("您没有权限更新咨询客户信息");
            }
            
            System.out.println("更新咨询客户，ID: " + id + ", 状态: " + request.getStatus());
            ConsultationClient client = convertToEntity(request);
            System.out.println("转换后的实体状态: " + client.getStatus());
            
            // 如果是顾问或文案，确保不能修改分配给其他人的客户
            if (!permissionService.isAdmin()) {
                if (permissionService.isCounselor()) {
                    // 顾问更新时，如果修改了顾问ID，确保只能设置为当前顾问
                    if (client.getCounselorId() != null && !client.getCounselorId().equals(permissionService.getCurrentUser().getId())) {
                        return ApiResponse.error("您只能将客户分配给自己");
                    }
                } else if (permissionService.isWriter()) {
                    // 文案更新时，如果修改了文案ID，确保只能设置为当前文案
                    if (client.getWriterId() != null && !client.getWriterId().equals(permissionService.getCurrentUser().getId())) {
                        return ApiResponse.error("您只能将客户分配给自己");
                    }
                }
            }
            
            ConsultationClient updatedClient = consultationClientService.updateConsultationClient(id, client);
            
            // 检查状态是否变为签约客户
            ConsultationClient.ClientStatus newStatus = updatedClient.getStatus();
            if (newStatus == ConsultationClient.ClientStatus.签约客户) {
                return ApiResponse.success("客户信息已成功复制到签约客户系统，咨询客户信息已保留", updatedClient);
            }
            
            return ApiResponse.success("咨询客户更新成功", updatedClient);
        } catch (Exception e) {
            System.err.println("更新咨询客户时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteConsultationClient(@PathVariable Long id) {
        try {
            // 权限控制：只有超级管理员可以删除咨询客户
            if (!permissionService.isSuperAdmin()) {
                return ApiResponse.error("只有超级管理员可以删除咨询客户");
            }
            
            consultationClientService.deleteConsultationClient(id);
            return ApiResponse.success("咨询客户删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 高级搜索咨询客户
     */
    @PostMapping("/advanced-search")
    public ApiResponse<?> advancedSearch(@RequestBody java.util.Map<String, Object> searchParams,
                                         @RequestParam(required = false, defaultValue = "0") int page,
                                         @RequestParam(required = false, defaultValue = "10") int size) {
        try {
            // 权限控制：管理员、咨询顾问和文案可以搜索
            if (!permissionService.isAdmin() && !permissionService.isCounselor() && !permissionService.isWriter()) {
                return ApiResponse.error("您没有权限搜索咨询客户");
            }
            
            String name = searchParams.get("name") != null ? searchParams.get("name").toString() : null;
            String idCard = searchParams.get("idCard") != null ? searchParams.get("idCard").toString() : null;
            String contact = searchParams.get("contact") != null ? searchParams.get("contact").toString() : null;
            String gender = searchParams.get("gender") != null ? searchParams.get("gender").toString() : null;
            String currentSchool = searchParams.get("currentSchool") != null ? searchParams.get("currentSchool").toString() : null;
            String major = searchParams.get("major") != null ? searchParams.get("major").toString() : null;
            String intendedCountry = searchParams.get("intendedCountry") != null ? searchParams.get("intendedCountry").toString() : null;
            String channelSource = searchParams.get("channelSource") != null ? searchParams.get("channelSource").toString() : null;
            ConsultationClient.ClientStatus status = null;
            if (searchParams.get("status") != null && !searchParams.get("status").toString().isEmpty()) {
                String statusStr = searchParams.get("status").toString();
                // 直接使用中文值匹配枚举（枚举值就是中文）
                for (ConsultationClient.ClientStatus s : ConsultationClient.ClientStatus.values()) {
                    if (s.name().equals(statusStr)) {
                        status = s;
                        break;
                    }
                }
            }
            java.time.LocalDate consultationDateStart = null;
            if (searchParams.get("consultationDateStart") != null && !searchParams.get("consultationDateStart").toString().isEmpty()) {
                consultationDateStart = java.time.LocalDate.parse(searchParams.get("consultationDateStart").toString());
            }
            java.time.LocalDate consultationDateEnd = null;
            if (searchParams.get("consultationDateEnd") != null && !searchParams.get("consultationDateEnd").toString().isEmpty()) {
                consultationDateEnd = java.time.LocalDate.parse(searchParams.get("consultationDateEnd").toString());
            }
            
            Page<ConsultationClient> clientPage = consultationClientService.advancedSearch(
                    name, idCard, contact, gender,
                    currentSchool, major, intendedCountry, channelSource, status,
                    consultationDateStart, consultationDateEnd,
                    page, size);
            
            return ApiResponse.success(clientPage);
        } catch (Exception e) {
            return ApiResponse.error("搜索失败: " + e.getMessage());
        }
    }
}





