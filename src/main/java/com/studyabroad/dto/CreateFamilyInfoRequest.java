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
    private String fatherContact;
    private String fatherWorkInfo;
    private String fatherEducation;
    private BigDecimal fatherIncome;
    private String motherName;
    private LocalDate motherBirthDate;
    private String motherContact;
    private String motherWorkInfo;
    private String motherEducation;
    private BigDecimal motherIncome;
    private BigDecimal annualIncome;
    private BigDecimal realEstateValue;
    private BigDecimal carValue;
    private BigDecimal stockValue;
    private BigDecimal fundValue;
    private BigDecimal depositValue;
    private BigDecimal otherInvestmentValue;
    private BigDecimal totalAssets;
    private String siblingsInfo;
}