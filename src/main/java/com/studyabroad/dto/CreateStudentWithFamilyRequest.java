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
        private String fatherPhone;
        private String fatherOccupation;
        private String fatherCompany;
        
        // 母亲信息
        private String motherName;
        private String motherPhone;
        private String motherOccupation;
        private String motherCompany;
        
        // 家庭资产信息
        private BigDecimal annualIncome;
        private BigDecimal realEstateValue;
        private BigDecimal carValue;
        private BigDecimal stockValue;
        private BigDecimal fundValue;
        private BigDecimal depositValue;
        private BigDecimal otherInvestmentValue;
        
        // 其他家庭信息
        private String familyAddress;
        private Integer familySize;
        private String emergencyContactName;
        private String emergencyContactPhone;
        private String emergencyContactRelation;
        private String notes;
    }
}


