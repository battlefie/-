package com.studyabroad.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 大学信息实体类
 */
@Entity
@Table(name = "universities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class University {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @Column(name = "shanghai_ranking")
    private Integer shanghaiRanking;

    @Column(name = "times_ranking")
    private Integer timesRanking;

    @Column(name = "qs_ranking")
    private Integer qsRanking;

    @Column(name = "us_news_ranking")
    private Integer usNewsRanking;

    @Column(name = "tuition_fee")
    private BigDecimal tuitionFee;

    @Column(name = "language_requirement", columnDefinition = "TEXT")
    private String languageRequirement;

    @Column(name = "gpa_requirement")
    private BigDecimal gpaRequirement;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String website;

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
}

