package com.studyabroad.repository;

import com.studyabroad.entity.ConsultationClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 咨询客户数据访问接口
 */
@Repository
public interface ConsultationClientRepository extends JpaRepository<ConsultationClient, Long> {
    List<ConsultationClient> findByStatus(ConsultationClient.ClientStatus status);
    
    // 根据顾问ID查询，按咨询时间降序排序
    @Query("SELECT c FROM ConsultationClient c LEFT JOIN FETCH c.counselor LEFT JOIN FETCH c.writer WHERE c.counselor.id = :counselorId ORDER BY c.consultationDate DESC, c.id DESC")
    Page<ConsultationClient> findByCounselorIdOrderByCreateTimeDesc(@Param("counselorId") Long counselorId, Pageable pageable);
    
    @Query("SELECT c FROM ConsultationClient c LEFT JOIN FETCH c.counselor LEFT JOIN FETCH c.writer WHERE c.counselor.id = :counselorId")
    List<ConsultationClient> findByCounselorId(@Param("counselorId") Long counselorId);
    
    // 根据文案ID查询，按咨询时间降序排序
    @Query("SELECT c FROM ConsultationClient c LEFT JOIN FETCH c.counselor LEFT JOIN FETCH c.writer WHERE c.writer.id = :writerId ORDER BY c.consultationDate DESC, c.id DESC")
    Page<ConsultationClient> findByWriterIdOrderByCreateTimeDesc(@Param("writerId") Long writerId, Pageable pageable);
    
    @Query("SELECT c FROM ConsultationClient c LEFT JOIN FETCH c.counselor LEFT JOIN FETCH c.writer WHERE c.writer.id = :writerId")
    List<ConsultationClient> findByWriterId(@Param("writerId") Long writerId);
    
    // 根据顾问ID和文案ID查询（用于同时满足两个条件），按咨询时间降序排序
    @Query("SELECT c FROM ConsultationClient c LEFT JOIN FETCH c.counselor LEFT JOIN FETCH c.writer WHERE (c.counselor.id = :userId OR c.writer.id = :userId) ORDER BY c.consultationDate DESC, c.id DESC")
    Page<ConsultationClient> findByCounselorIdOrWriterIdOrderByCreateTimeDesc(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT c FROM ConsultationClient c LEFT JOIN FETCH c.counselor LEFT JOIN FETCH c.writer WHERE (c.counselor.id = :userId OR c.writer.id = :userId)")
    List<ConsultationClient> findByCounselorIdOrWriterId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM ConsultationClient c LEFT JOIN FETCH c.counselor LEFT JOIN FETCH c.writer ORDER BY c.consultationDate DESC, c.id DESC")
    Page<ConsultationClient> findAllByOrderByCreateTimeDesc(Pageable pageable);
    
    @Query("SELECT c FROM ConsultationClient c LEFT JOIN FETCH c.counselor LEFT JOIN FETCH c.writer WHERE c.id = :id")
    Optional<ConsultationClient> findByIdWithUsers(Long id);
    
    /**
     * 高级搜索：根据多个条件查询咨询客户，按咨询时间降序排序
     */
    @Query("SELECT DISTINCT c FROM ConsultationClient c " +
           "LEFT JOIN FETCH c.counselor LEFT JOIN FETCH c.writer " +
           "WHERE (:name IS NULL OR c.name LIKE CONCAT('%', :name, '%')) " +
           "AND (:idCard IS NULL OR c.idCard LIKE CONCAT('%', :idCard, '%')) " +
           "AND (:contact IS NULL OR c.contactInfo LIKE CONCAT('%', :contact, '%')) " +
           "AND (:gender IS NULL OR c.genderStr = :gender) " +
           "AND (:currentSchool IS NULL OR c.currentSchool LIKE CONCAT('%', :currentSchool, '%')) " +
           "AND (:major IS NULL OR c.major LIKE CONCAT('%', :major, '%')) " +
           "AND (:intendedCountry IS NULL OR c.intendedCountry LIKE CONCAT('%', :intendedCountry, '%')) " +
           "AND (:channelSource IS NULL OR c.channelSource LIKE CONCAT('%', :channelSource, '%')) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:consultationDateStart IS NULL OR c.consultationDate >= :consultationDateStart) " +
           "AND (:consultationDateEnd IS NULL OR c.consultationDate <= :consultationDateEnd) " +
           "ORDER BY c.consultationDate DESC, c.id DESC")
    Page<ConsultationClient> advancedSearch(
            @Param("name") String name,
            @Param("idCard") String idCard,
            @Param("contact") String contact,
            @Param("gender") String gender,
            @Param("currentSchool") String currentSchool,
            @Param("major") String major,
            @Param("intendedCountry") String intendedCountry,
            @Param("channelSource") String channelSource,
            @Param("status") ConsultationClient.ClientStatus status,
            @Param("consultationDateStart") java.time.LocalDate consultationDateStart,
            @Param("consultationDateEnd") java.time.LocalDate consultationDateEnd,
            Pageable pageable);
    
    /**
     * 统计顾问每年的签约转化率
     * 返回：顾问ID、年份、总咨询客户数、签约客户数
     */
    @Query(value = "SELECT " +
           "c.counselor_id as counselorId, " +
           "YEAR(c.consultation_date) as year, " +
           "COUNT(*) as totalClients, " +
           "SUM(CASE WHEN c.status = '签约客户' THEN 1 ELSE 0 END) as signedClients " +
           "FROM consultation_clients c " +
           "WHERE c.counselor_id IS NOT NULL " +
           "AND c.consultation_date IS NOT NULL " +
           "GROUP BY c.counselor_id, YEAR(c.consultation_date) " +
           "ORDER BY c.counselor_id, YEAR(c.consultation_date) DESC", 
           nativeQuery = true)
    List<Object[]> getCounselorConversionRateStatistics();
}





