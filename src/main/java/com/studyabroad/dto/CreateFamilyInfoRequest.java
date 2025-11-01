package com.studyabroad.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建家庭信息请求DTO
 */
@Data
public class CreateFamilyInfoRequest {
    private Long studentId;
    private String fatherName;
    private LocalDate fatherBirthDate;
    private String fatherPhone;
    private String fatherOccupation;
    private String fatherCompany;
    private BigDecimal fatherIncome;
    private String motherName;
    private LocalDate motherBirthDate;
    private String motherPhone;
    private String motherOccupation;
    private String motherCompany;
    private BigDecimal motherIncome;
    private BigDecimal annualIncome;
    private BigDecimal realEstateValue;
    private BigDecimal carValue;
    private BigDecimal stockValue;
    private BigDecimal fundValue;
    private BigDecimal depositValue;
    private BigDecimal otherInvestmentValue;
    private BigDecimal totalAssets;
    private String familyAddress;
    private Integer familySize;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private String notes;
}