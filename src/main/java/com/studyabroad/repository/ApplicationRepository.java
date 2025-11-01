package com.studyabroad.repository;

import com.studyabroad.entity.Application;
import com.studyabroad.entity.Student;
import com.studyabroad.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 申请数据访问接口
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStudent(Student student);
    
    List<Application> findByStudentId(Long studentId);
    
    List<Application> findByUniversityName(String universityName);
    
    List<Application> findByStatus(Application.ApplicationStatus status);
    
    List<Application> findByCounselor(User counselor);
    
    Page<Application> findByCounselor(User counselor, Pageable pageable);
    
    List<Application> findByWriter(User writer);
    
    Page<Application> findByWriter(User writer, Pageable pageable);
    
    List<Application> findAllByOrderByCreateTimeDesc();
}

