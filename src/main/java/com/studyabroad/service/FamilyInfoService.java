package com.studyabroad.service;

import com.studyabroad.entity.FamilyInfo;
import com.studyabroad.repository.FamilyInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 家庭信息服务类
 */
@Service
public class FamilyInfoService {

    private final FamilyInfoRepository familyInfoRepository;

    public FamilyInfoService(FamilyInfoRepository familyInfoRepository) {
        this.familyInfoRepository = familyInfoRepository;
    }

    @Transactional
    public FamilyInfo createFamilyInfo(FamilyInfo familyInfo) {
        return familyInfoRepository.save(familyInfo);
    }

    public List<FamilyInfo> getAllFamilyInfo() {
        return familyInfoRepository.findAll();
    }

    public Optional<FamilyInfo> getFamilyInfoById(Long id) {
        return familyInfoRepository.findById(id);
    }

    public Optional<FamilyInfo> getFamilyInfoByStudentId(Long studentId) {
        return familyInfoRepository.findByStudentId(studentId);
    }

    public boolean existsByStudentId(Long studentId) {
        return familyInfoRepository.existsByStudentId(studentId);
    }

    @Transactional
    public FamilyInfo updateFamilyInfo(Long id, FamilyInfo familyInfoDetails) {
        FamilyInfo familyInfo = familyInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("家庭信息未找到，ID: " + id));

        // 更新字段
        if (familyInfoDetails.getFatherName() != null) familyInfo.setFatherName(familyInfoDetails.getFatherName());
        if (familyInfoDetails.getFatherContact() != null) familyInfo.setFatherContact(familyInfoDetails.getFatherContact());
        if (familyInfoDetails.getFatherWorkInfo() != null) familyInfo.setFatherWorkInfo(familyInfoDetails.getFatherWorkInfo());
        if (familyInfoDetails.getFatherEducation() != null) familyInfo.setFatherEducation(familyInfoDetails.getFatherEducation());

        if (familyInfoDetails.getMotherName() != null) familyInfo.setMotherName(familyInfoDetails.getMotherName());
        if (familyInfoDetails.getMotherContact() != null) familyInfo.setMotherContact(familyInfoDetails.getMotherContact());
        if (familyInfoDetails.getMotherWorkInfo() != null) familyInfo.setMotherWorkInfo(familyInfoDetails.getMotherWorkInfo());
        if (familyInfoDetails.getMotherEducation() != null) familyInfo.setMotherEducation(familyInfoDetails.getMotherEducation());

        if (familyInfoDetails.getAnnualIncome() != null) familyInfo.setAnnualIncome(familyInfoDetails.getAnnualIncome());
        if (familyInfoDetails.getRealEstateValue() != null) familyInfo.setRealEstateValue(familyInfoDetails.getRealEstateValue());
        if (familyInfoDetails.getCarValue() != null) familyInfo.setCarValue(familyInfoDetails.getCarValue());
        if (familyInfoDetails.getStockValue() != null) familyInfo.setStockValue(familyInfoDetails.getStockValue());
        if (familyInfoDetails.getFundValue() != null) familyInfo.setFundValue(familyInfoDetails.getFundValue());
        if (familyInfoDetails.getDepositValue() != null) familyInfo.setDepositValue(familyInfoDetails.getDepositValue());
        if (familyInfoDetails.getOtherInvestmentValue() != null) familyInfo.setOtherInvestmentValue(familyInfoDetails.getOtherInvestmentValue());

        if (familyInfoDetails.getSiblingsInfo() != null) familyInfo.setSiblingsInfo(familyInfoDetails.getSiblingsInfo());

        // 自动计算总资产
        familyInfo.calculateTotalAssets();

        return familyInfoRepository.save(familyInfo);
    }

    @Transactional
    public void deleteFamilyInfo(Long id) {
        familyInfoRepository.deleteById(id);
    }
}
