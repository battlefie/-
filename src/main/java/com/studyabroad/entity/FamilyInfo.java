package com.studyabroad.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 家庭信息实体类
 */
@Data
@Entity
@Table(name = "family_info")
public class FamilyInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "student_id", unique = true)
    @JsonIgnore
    private Student student;

    @Column(name = "father_name", length = 100)
    private String fatherName;

    @Column(name = "father_birth_date")
    private LocalDate fatherBirthDate;

    @Column(name = "father_phone", length = 20)
    private String fatherPhone;

    @Column(name = "father_occupation", length = 100)
    private String fatherOccupation;

    @Column(name = "father_company", length = 200)
    private String fatherCompany;

    @Column(name = "father_income")
    private BigDecimal fatherIncome;

    @Column(name = "mother_name", length = 100)
    private String motherName;

    @Column(name = "mother_birth_date")
    private LocalDate motherBirthDate;

    @Column(name = "mother_phone", length = 20)
    private String motherPhone;

    @Column(name = "mother_occupation", length = 100)
    private String motherOccupation;

    @Column(name = "mother_company", length = 200)
    private String motherCompany;

    @Column(name = "mother_income")
    private BigDecimal motherIncome;

    @Column(name = "annual_income")
    private BigDecimal annualIncome;

    @Column(name = "real_estate_value")
    private BigDecimal realEstateValue;

    @Column(name = "car_value")
    private BigDecimal carValue;

    @Column(name = "stock_value")
    private BigDecimal stockValue;

    @Column(name = "fund_value")
    private BigDecimal fundValue;

    @Column(name = "deposit_value")
    private BigDecimal depositValue;

    @Column(name = "other_investment_value")
    private BigDecimal otherInvestmentValue;

    @Column(name = "total_assets")
    private BigDecimal totalAssets;

    @Column(name = "family_address", length = 500)
    private String familyAddress;

    @Column(name = "family_size")
    private Integer familySize;

    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relation", length = 50)
    private String emergencyContactRelation;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    /**
     * 计算总资产
     */
    public BigDecimal calculateTotalAssets() {
        BigDecimal total = BigDecimal.ZERO;
        if (realEstateValue != null) total = total.add(realEstateValue);
        if (carValue != null) total = total.add(carValue);
        if (stockValue != null) total = total.add(stockValue);
        if (fundValue != null) total = total.add(fundValue);
        if (depositValue != null) total = total.add(depositValue);
        if (otherInvestmentValue != null) total = total.add(otherInvestmentValue);
        return total;
    }
}