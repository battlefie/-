package com.studyabroad.service;

import com.studyabroad.entity.University;
import com.studyabroad.repository.UniversityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 大学服务类
 */
@Service
public class UniversityService {

    private final UniversityRepository universityRepository;

    public UniversityService(UniversityRepository universityRepository) {
        this.universityRepository = universityRepository;
    }

    @Transactional
    public University createUniversity(University university) {
        return universityRepository.save(university);
    }

    public University getUniversityById(Long id) {
        return universityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("大学信息不存在"));
    }

    public List<University> getAllUniversities() {
        return universityRepository.findAll();
    }

    /**
     * 分页获取所有大学
     */
    public Page<University> getUniversities(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return universityRepository.findAll(pageable);
    }

    public List<University> getUniversitiesByCountry(String country) {
        return universityRepository.findByCountry(country);
    }

    public List<University> searchUniversitiesByName(String name) {
        return universityRepository.findByNameContaining(name);
    }

    @Transactional
    public University updateUniversity(Long id, University universityDetails) {
        University university = getUniversityById(id);
        
        if (universityDetails.getName() != null) university.setName(universityDetails.getName());
        if (universityDetails.getCountry() != null) university.setCountry(universityDetails.getCountry());
        if (universityDetails.getCity() != null) university.setCity(universityDetails.getCity());
        if (universityDetails.getShanghaiRanking() != null) university.setShanghaiRanking(universityDetails.getShanghaiRanking());
        if (universityDetails.getTimesRanking() != null) university.setTimesRanking(universityDetails.getTimesRanking());
        if (universityDetails.getQsRanking() != null) university.setQsRanking(universityDetails.getQsRanking());
        if (universityDetails.getUsNewsRanking() != null) university.setUsNewsRanking(universityDetails.getUsNewsRanking());
        if (universityDetails.getWebsite() != null) university.setWebsite(universityDetails.getWebsite());
        if (universityDetails.getDescription() != null) university.setDescription(universityDetails.getDescription());
        if (universityDetails.getApplicationDeadline() != null) university.setApplicationDeadline(universityDetails.getApplicationDeadline());
        if (universityDetails.getTuitionFee() != null) university.setTuitionFee(universityDetails.getTuitionFee());
        if (universityDetails.getLanguageRequirement() != null) university.setLanguageRequirement(universityDetails.getLanguageRequirement());
        if (universityDetails.getGpaRequirement() != null) university.setGpaRequirement(universityDetails.getGpaRequirement());
        
        return universityRepository.save(university);
    }

    @Transactional
    public void deleteUniversity(Long id) {
        universityRepository.deleteById(id);
    }
}

