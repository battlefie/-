package com.studyabroad.repository;

import com.studyabroad.entity.Student;
import com.studyabroad.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学生数据访问接口
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    /**
     * 根据咨询顾问查找学生
     */
    List<Student> findByCounselor(User counselor);
    
    /**
     * 分页查询所有学生
     */
    Page<Student> findAll(Pageable pageable);
    
    /**
     * 根据咨询顾问分页查找学生
     */
    Page<Student> findByCounselor(User counselor, Pageable pageable);
    
    /**
     * 根据咨询顾问分页查找学生，按签约时间降序排序
     * 如果签约时间相同，按创建时间降序排序，确保新转换的客户显示在最前面
     */
    @Query("SELECT s FROM Student s WHERE s.counselor = :counselor ORDER BY s.contractDate DESC, s.createTime DESC, s.id DESC")
    Page<Student> findByCounselorOrderByContractDateDesc(@Param("counselor") User counselor, Pageable pageable);
    
    /**
     * 根据文案分页查找学生
     */
    Page<Student> findByWriter(User writer, Pageable pageable);
    
    /**
     * 根据文案分页查找学生，按签约时间降序排序
     * 如果签约时间相同，按创建时间降序排序，确保新转换的客户显示在最前面
     */
    @Query("SELECT s FROM Student s WHERE s.writer = :writer ORDER BY s.contractDate DESC, s.createTime DESC, s.id DESC")
    Page<Student> findByWriterOrderByContractDateDesc(@Param("writer") User writer, Pageable pageable);
    
    /**
     * 根据文案查找学生
     */
    List<Student> findByWriter(User writer);
    
    /**
     * 根据姓名模糊查询
     */
    List<Student> findByNameContainingIgnoreCase(String name);
    
    /**
     * 根据姓名模糊分页查询
     */
    Page<Student> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * 根据身份证号查找学生
     */
    Optional<Student> findByIdCard(String idCard);
    
    /**
     * 根据咨询顾问和姓名模糊查询
     */
    @Query("SELECT s FROM Student s WHERE s.counselor = :counselor AND s.name LIKE %:name%")
    List<Student> findByCounselorAndNameContaining(@Param("counselor") User counselor, @Param("name") String name);
    
    /**
     * 根据咨询顾问和姓名分页模糊查询
     */
    @Query("SELECT s FROM Student s WHERE s.counselor = :counselor AND s.name LIKE %:name%")
    Page<Student> findByCounselorAndNameContaining(@Param("counselor") User counselor, @Param("name") String name, Pageable pageable);
    
    /**
     * 根据文案和姓名模糊查询
     */
    @Query("SELECT s FROM Student s WHERE s.writer = :writer AND s.name LIKE %:name%")
    List<Student> findByWriterAndNameContaining(@Param("writer") User writer, @Param("name") String name);
    
    /**
     * 根据文案和姓名分页模糊查询
     */
    @Query("SELECT s FROM Student s WHERE s.writer = :writer AND s.name LIKE %:name%")
    Page<Student> findByWriterAndNameContaining(@Param("writer") User writer, @Param("name") String name, Pageable pageable);
    
    /**
     * 根据学生状态查找学生
     */
    @Query("SELECT s FROM Student s WHERE s.statusStr = :value")
    List<Student> findByStatus(@Param("value") String value);
    
    /**
     * 高级搜索：根据多个条件查询学生
     */
    @Query("SELECT DISTINCT s FROM Student s " +
           "LEFT JOIN FamilyInfo fi ON fi.student.id = s.id " +
           "WHERE (:name IS NULL OR s.name LIKE CONCAT('%', :name, '%')) " +
           "AND (:idCard IS NULL OR s.idCard LIKE CONCAT('%', :idCard, '%')) " +
           "AND (:contact IS NULL OR s.contactInfo LIKE CONCAT('%', :contact, '%')) " +
           "AND (:gender IS NULL OR s.genderStr = :gender) " +
           "AND (:currentSchool IS NULL OR s.currentSchool LIKE CONCAT('%', :currentSchool, '%')) " +
           "AND (:major IS NULL OR s.major LIKE CONCAT('%', :major, '%')) " +
           "AND (:intendedCountry IS NULL OR s.intendedCountry LIKE CONCAT('%', :intendedCountry, '%')) " +
           "AND (:status IS NULL OR s.statusStr = :status) " +
           "AND (:fatherName IS NULL OR fi.fatherName LIKE CONCAT('%', :fatherName, '%')) " +
           "AND (:fatherContact IS NULL OR fi.fatherContact LIKE CONCAT('%', :fatherContact, '%')) " +
           "AND (:fatherWorkInfo IS NULL OR fi.fatherWorkInfo LIKE CONCAT('%', :fatherWorkInfo, '%')) " +
           "AND (:motherName IS NULL OR fi.motherName LIKE CONCAT('%', :motherName, '%')) " +
           "AND (:motherContact IS NULL OR fi.motherContact LIKE CONCAT('%', :motherContact, '%')) " +
           "AND (:motherWorkInfo IS NULL OR fi.motherWorkInfo LIKE CONCAT('%', :motherWorkInfo, '%')) " +
           "AND (:contractDateStart IS NULL OR s.contractDate >= :contractDateStart) " +
           "AND (:contractDateEnd IS NULL OR s.contractDate <= :contractDateEnd)")
    Page<Student> advancedSearch(
            @Param("name") String name,
            @Param("idCard") String idCard,
            @Param("contact") String contact,
            @Param("gender") String gender,
            @Param("currentSchool") String currentSchool,
            @Param("major") String major,
            @Param("intendedCountry") String intendedCountry,
            @Param("status") String status,
            @Param("fatherName") String fatherName,
            @Param("fatherContact") String fatherContact,
            @Param("fatherWorkInfo") String fatherWorkInfo,
            @Param("motherName") String motherName,
            @Param("motherContact") String motherContact,
            @Param("motherWorkInfo") String motherWorkInfo,
            @Param("contractDateStart") java.time.LocalDate contractDateStart,
            @Param("contractDateEnd") java.time.LocalDate contractDateEnd,
            Pageable pageable);
}
