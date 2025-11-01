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
     * 根据文案分页查找学生
     */
    Page<Student> findByWriter(User writer, Pageable pageable);
    
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
     * 根据邮箱查找学生
     */
    Optional<Student> findByEmail(String email);
    
    /**
     * 根据手机号查找学生
     */
    Optional<Student> findByPhone(String phone);
    
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
     * 根据学生来源查找学生
     */
    @Query("SELECT s FROM Student s WHERE s.studentSourceStr = :value")
    List<Student> findByStudentSource(@Param("value") String value);
    
    /**
     * 根据学生状态查找学生
     */
    @Query("SELECT s FROM Student s WHERE s.statusStr = :value")
    List<Student> findByStatus(@Param("value") String value);
}
