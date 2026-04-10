package com.studyabroad.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 创建学生（包含用户和家庭信息）请求DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CreateStudentWithFamilyRequest extends CreateStudentWithUserRequest {

    private FamilyInfoDto familyInfo;

    @Data
    public static class FamilyInfoDto {
        // 父亲信息
        private String fatherName;
        private String fatherContact;
        private String fatherWorkInfo;
        private String fatherEducation;
        
        // 母亲信息
        private String motherName;
        private String motherContact;
        private String motherWorkInfo;
        private String motherEducation;
        
        // 家庭资产信息
        private BigDecimal annualIncome;
        private BigDecimal realEstateValue;
        private BigDecimal carValue;
        private BigDecimal stockValue;
        private BigDecimal fundValue;
        private BigDecimal depositValue;
        private BigDecimal otherInvestmentValue;
        private BigDecimal totalAssets;
        
        // 兄弟姐妹信息
        private String siblingsInfo;
    }
}


