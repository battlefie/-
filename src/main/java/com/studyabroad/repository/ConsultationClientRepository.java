package com.studyabroad.repository;

import com.studyabroad.entity.ConsultationClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 咨询客户数据访问接口
 */
@Repository
public interface ConsultationClientRepository extends JpaRepository<ConsultationClient, Long> {
    List<ConsultationClient> findByStatus(ConsultationClient.ClientStatus status);
    
    Page<ConsultationClient> findAllByOrderByCreateTimeDesc(Pageable pageable);
}





