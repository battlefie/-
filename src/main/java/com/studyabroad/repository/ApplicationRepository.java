package com.studyabroad.repository;

import com.studyabroad.entity.Application;
import com.studyabroad.entity.Student;
import com.studyabroad.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 申请数据访问接口
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStudent(Student student);
    
    List<Application> findByStudentId(Long studentId);
    
    Page<Application> findByStudentId(Long studentId, Pageable pageable);

    List<Application> findByStudentIdIn(List<Long> studentIds);
    
    List<Application> findByUniversityName(String universityName);
    
    List<Application> findByStatus(Application.ApplicationStatus status);
    
    List<Application> findByCounselor(User counselor);
    
    Page<Application> findByCounselor(User counselor, Pageable pageable);
    
    List<Application> findByWriter(User writer);
    
    Page<Application> findByWriter(User writer, Pageable pageable);
    
    /**
     * 根据学生的文案ID查询申请（业务规则：文案负责申请，申请属于学生，学生由文案负责）
     * 通过关联查询，找到所有学生的writer_id等于指定writerId的申请
     */
    @Query("SELECT a FROM Application a WHERE a.student.writer.id = :writerId")
    List<Application> findByStudentWriterId(@Param("writerId") Long writerId);
    
    /**
     * 分页查询：根据学生的文案ID查询申请
     */
    @Query("SELECT a FROM Application a WHERE a.student.writer.id = :writerId")
    Page<Application> findByStudentWriterIdPage(@Param("writerId") Long writerId, Pageable pageable);
    
    List<Application> findAllByOrderByCreateTimeDesc();
}

