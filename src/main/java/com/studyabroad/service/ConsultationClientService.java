package com.studyabroad.service;

import com.studyabroad.entity.ConsultationClient;
import com.studyabroad.repository.ConsultationClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 咨询客户服务类
 */
@Service
public class ConsultationClientService {

    private final ConsultationClientRepository consultationClientRepository;

    public ConsultationClientService(ConsultationClientRepository consultationClientRepository) {
        this.consultationClientRepository = consultationClientRepository;
    }

    /**
     * 创建咨询客户
     */
    @Transactional
    public ConsultationClient createConsultationClient(ConsultationClient client) {
        return consultationClientRepository.save(client);
    }

    /**
     * 获取所有咨询客户
     */
    public List<ConsultationClient> getAllConsultationClients() {
        return consultationClientRepository.findAll();
    }

    /**
     * 分页获取所有咨询客户
     */
    public Page<ConsultationClient> getConsultationClients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        return consultationClientRepository.findAllByOrderByCreateTimeDesc(pageable);
    }

    /**
     * 根据ID获取咨询客户
     */
    public ConsultationClient getConsultationClientById(Long id) {
        return consultationClientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("咨询客户不存在"));
    }

    /**
     * 根据状态获取咨询客户
     */
    public List<ConsultationClient> getConsultationClientsByStatus(ConsultationClient.ClientStatus status) {
        return consultationClientRepository.findByStatus(status);
    }

    /**
     * 更新咨询客户
     */
    @Transactional
    public ConsultationClient updateConsultationClient(Long id, ConsultationClient clientDetails) {
        ConsultationClient client = getConsultationClientById(id);
        
        if (clientDetails.getName() != null) client.setName(clientDetails.getName());
        if (clientDetails.getContactPhone() != null) client.setContactPhone(clientDetails.getContactPhone());
        if (clientDetails.getStatus() != null) client.setStatus(clientDetails.getStatus());
        if (clientDetails.getConsultationDate() != null) client.setConsultationDate(clientDetails.getConsultationDate());
        if (clientDetails.getGender() != null) client.setGender(clientDetails.getGender());
        if (clientDetails.getChannel() != null) client.setChannel(clientDetails.getChannel());
        if (clientDetails.getTargetCountry() != null) client.setTargetCountry(clientDetails.getTargetCountry());
        if (clientDetails.getTargetDegree() != null) client.setTargetDegree(clientDetails.getTargetDegree());
        if (clientDetails.getGraduationDate() != null) client.setGraduationDate(clientDetails.getGraduationDate());
        if (clientDetails.getEnglishScore() != null) client.setEnglishScore(clientDetails.getEnglishScore());
        if (clientDetails.getCurrentSchool() != null) client.setCurrentSchool(clientDetails.getCurrentSchool());
        if (clientDetails.getMajor() != null) client.setMajor(clientDetails.getMajor());
        if (clientDetails.getHomeAddress() != null) client.setHomeAddress(clientDetails.getHomeAddress());
        if (clientDetails.getEmail() != null) client.setEmail(clientDetails.getEmail());
        if (clientDetails.getNotes() != null) client.setNotes(clientDetails.getNotes());
        if (clientDetails.getFollowUpStatus() != null) client.setFollowUpStatus(clientDetails.getFollowUpStatus());
        
        return consultationClientRepository.save(client);
    }

    /**
     * 删除咨询客户
     */
    @Transactional
    public void deleteConsultationClient(Long id) {
        consultationClientRepository.deleteById(id);
    }
}





