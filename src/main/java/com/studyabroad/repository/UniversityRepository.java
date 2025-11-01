package com.studyabroad.repository;

import com.studyabroad.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 大学数据访问接口
 */
@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
    List<University> findByCountry(String country);
    
    List<University> findByNameContaining(String name);
    
    List<University> findByCountryAndCity(String country, String city);
}

