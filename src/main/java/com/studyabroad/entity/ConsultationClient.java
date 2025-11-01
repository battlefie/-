package com.studyabroad.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 咨询客户实体类
 */
@Entity
@Table(name = "consultation_clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column
    private ClientStatus status;

    @Column(name = "consultation_date")
    private LocalDate consultationDate;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;

    @Column(length = 100)
    private String channel;

    @Column(name = "target_country", length = 100)
    private String targetCountry;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_degree")
    private TargetDegree targetDegree;

    @Column(name = "graduation_date")
    private LocalDate graduationDate;

    @Column(name = "english_score", length = 50)
    private String englishScore;

    @Column(name = "current_school", length = 200)
    private String currentSchool;

    @Column(length = 100)
    private String major;

    @Column(name = "home_address", length = 500)
    private String homeAddress;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "follow_up_status", columnDefinition = "TEXT")
    private String followUpStatus;

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
     * 客户状态枚举
     */
    public enum ClientStatus {
        潜在客户, 意向客户, 签约客户, 已流失
    }

    /**
     * 性别枚举
     */
    public enum Gender {
        男, 女
    }

    /**
     * 意向学位级别枚举
     */
    public enum TargetDegree {
        JUNIOR_HIGH, HIGH_SCHOOL, BACHELOR, MASTER, PHD
    }
}
