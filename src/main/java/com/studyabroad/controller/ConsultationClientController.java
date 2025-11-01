package com.studyabroad.controller;

import com.studyabroad.dto.ApiResponse;
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
    public ApiResponse<ConsultationClient> createConsultationClient(@RequestBody ConsultationClient client) {
        try {
            ConsultationClient createdClient = consultationClientService.createConsultationClient(client);
            return ApiResponse.success("咨询客户创建成功", createdClient);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<ConsultationClient> getConsultationClientById(@PathVariable Long id) {
        try {
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
            // 权限控制：只有管理员和咨询顾问可以查看
            if (!permissionService.isAdmin() && !permissionService.isCounselor()) {
                return ApiResponse.error("您没有权限访问咨询客户信息");
            }
            
            // 如果提供了分页参数，返回分页数据
            if (page != null && size != null) {
                Page<ConsultationClient> clientsPage = consultationClientService.getConsultationClients(page, size);
                return ApiResponse.success(clientsPage);
            }
            
            // 如果提供了状态筛选，返回按状态筛选的数据
            if (status != null) {
                List<ConsultationClient> clients = consultationClientService.getConsultationClientsByStatus(status);
                return ApiResponse.success(clients);
            }
            
            // 否则返回所有数据
            List<ConsultationClient> clients = consultationClientService.getAllConsultationClients();
            return ApiResponse.success(clients);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<ConsultationClient> updateConsultationClient(@PathVariable Long id, @RequestBody ConsultationClient client) {
        try {
            ConsultationClient updatedClient = consultationClientService.updateConsultationClient(id, client);
            return ApiResponse.success("咨询客户更新成功", updatedClient);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteConsultationClient(@PathVariable Long id) {
        try {
            consultationClientService.deleteConsultationClient(id);
            return ApiResponse.success("咨询客户删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}





