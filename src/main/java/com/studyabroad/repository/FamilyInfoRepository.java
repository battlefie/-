package com.studyabroad.repository;

import com.studyabroad.entity.FamilyInfo;
import com.studyabroad.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 家庭信息Repository
 */
@Repository
public interface FamilyInfoRepository extends JpaRepository<FamilyInfo, Long> {
    Optional<FamilyInfo> findByStudent(Student student);
    Optional<FamilyInfo> findByStudentId(Long studentId);
    boolean existsByStudentId(Long studentId);
    
    @Query("SELECT fi FROM FamilyInfo fi WHERE fi.student.id IN :studentIds")
    List<FamilyInfo> findByStudentIdIn(@Param("studentIds") List<Long> studentIds);
}
