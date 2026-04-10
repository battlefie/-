package com.studyabroad.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 留学申请实体类
 */
@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "university_name", nullable = false, length = 200)
    private String universityName;

    @Column(name = "university_email", length = 200)
    private String universityEmail;

    @Column(name = "university_email_password", length = 200)
    private String universityEmailPassword;

    @Column(length = 100)
    private String country;

    @Column(nullable = false, length = 100)
    private String major;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DegreeType degreeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "visa_submission_date")
    private LocalDate visaSubmissionDate;

    @Column(name = "interview_date")
    private LocalDate interviewDate;

    @Column(name = "fingerprint_collection_date")
    private LocalDate fingerprintCollectionDate;

    @Column(name = "medical_exam_date")
    private LocalDate medicalExamDate;

    @Column(name = "visa_approved_date")
    private LocalDate visaApprovedDate;

    @Column(name = "visa_rejected_date")
    private LocalDate visaRejectedDate;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Column(name = "airport_pickup_accommodation", columnDefinition = "TEXT")
    private String airportPickupAccommodation;

    @Column(name = "follow_up_status", columnDefinition = "TEXT")
    private String followUpStatus;

    @Column(name = "arrival_status", columnDefinition = "TEXT")
    private String arrivalStatus;

    @Column(name = "status_url", length = 500)
    private String statusUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "counselor_id")
    private User counselor;

    @ManyToOne
    @JoinColumn(name = "writer_id")
    private User writer;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"application", "fileContent"}) // 避免序列化大文件和循环引用
    private List<com.studyabroad.entity.Document> documents;

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
     * 学位类型枚举
     */
    public enum DegreeType {
        JUNIOR_HIGH,    // 初中
        HIGH_SCHOOL,    // 高中
        BACHELOR,       // 本科
        MASTER,         // 硕士
        PHD             // 博士
    }

    /**
     * 申请状态枚举
     */
    public enum ApplicationStatus {
        DRAFT,          // 草稿
        SUBMITTED,      // 已提交
        UNDER_REVIEW,   // 审核中
        ACCEPTED,       // 已录取
        REJECTED,       // 已拒绝
        WAITLISTED,     // 候补
        WITHDRAWN       // 已撤回
    }
}

