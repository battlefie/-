package com.studyabroad.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import com.studyabroad.dto.ApplicationSummary;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生实体类
 */
@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "status", length = 20)
    private String statusStr;
    
    public StudentStatus getStatus() {
        return statusStr != null ? StudentStatus.fromValue(statusStr) : null;
    }
    
    public void setStatus(StudentStatus status) {
        this.statusStr = status != null ? status.getValue() : null;
    }

    @Column(name = "gender", length = 10)
    private String genderStr;
    
    public Gender getGender() {
        return genderStr != null ? Gender.fromValue(genderStr) : null;
    }
    
    public void setGender(Gender gender) {
        this.genderStr = gender != null ? gender.getValue() : null;
    }

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "id_card", length = 20)
    private String idCard;

    @Column(length = 500)
    private String address;

    @Column(name = "contact_info", length = 500)
    private String contactInfo;

    @Column(name = "current_school", length = 200)
    private String currentSchool;

    @Column(name = "channel_source", length = 100)
    private String channelSource;

    @Column(name = "intended_country", length = 100)
    private String intendedCountry;

    @Column(name = "enrolled_country", length = 100)
    private String enrolledCountry;

    @Column(name = "enrolled_school", length = 200)
    private String enrolledSchool;

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "contract_amount")
    private Double contractAmount;

    @Column(length = 100)
    private String major;

    @Column
    private Double gpa;

    @Column(name = "language_scores", length = 200)
    private String languageScores;

    @Column(columnDefinition = "TEXT")
    private String awards;

    @Column(columnDefinition = "TEXT")
    private String experiences;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "counselor_id")
    private User counselor;

    @ManyToOne
    @JoinColumn(name = "writer_id")
    private User writer;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Transient
    private List<ApplicationSummary> applications;

    @Transient
    private FamilyInfo familyInfo;

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
     * 性别枚举
     */
    public enum Gender {
        MALE("男"), FEMALE("女");
        
        private final String value;
        
        Gender(String value) {
            this.value = value;
        }
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public static Gender fromValue(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            for (Gender gender : values()) {
                if (gender.value.equals(value)) {
                    return gender;
                }
            }
            // 如果没有匹配的值，返回null而不是抛出异常
            return null;
        }
    }

    /**
     * 学生来源枚举
     */
    public enum StudentSource {
        SOCIAL("社会生源"), SICHUAN_INTERNATIONAL("四中国际部"), AGRICULTURAL_UNIVERSITY("农大"), NO47_INTERNATIONAL("47国际部"), OTHER("其他");
        
        private final String value;
        
        StudentSource(String value) {
            this.value = value;
        }
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public static StudentSource fromValue(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            for (StudentSource source : values()) {
                if (source.value.equals(value)) {
                    return source;
                }
            }
            // 如果没有匹配的值，返回null而不是抛出异常
            return null;
        }
    }

    /**
     * 学生状态枚举
     */
    public enum StudentStatus {
        ACTIVE("在途"), INACTIVE("已退"), FINISHED("完结");
        
        private final String value;
        
        StudentStatus(String value) {
            this.value = value;
        }
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public static StudentStatus fromValue(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            for (StudentStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            // 如果没有匹配的值，返回null而不是抛出异常
            return null;
        }
    }

    /**
     * 性别转换器
     */
    public static class GenderConverter implements AttributeConverter<Gender, String> {
        @Override
        public String convertToDatabaseColumn(Gender attribute) {
            return attribute == null ? null : attribute.getValue();
        }

        @Override
        public Gender convertToEntityAttribute(String dbData) {
            return dbData == null ? null : Gender.fromValue(dbData);
        }
    }

    /**
     * 学生来源转换器
     */
    public static class StudentSourceConverter implements AttributeConverter<StudentSource, String> {
        @Override
        public String convertToDatabaseColumn(StudentSource attribute) {
            return attribute == null ? null : attribute.getValue();
        }

        @Override
        public StudentSource convertToEntityAttribute(String dbData) {
            return dbData == null ? null : StudentSource.fromValue(dbData);
        }
    }

    /**
     * 学生状态转换器
     */
    public static class StudentStatusConverter implements AttributeConverter<StudentStatus, String> {
        @Override
        public String convertToDatabaseColumn(StudentStatus attribute) {
            return attribute == null ? null : attribute.getValue();
        }

        @Override
        public StudentStatus convertToEntityAttribute(String dbData) {
            return dbData == null ? null : StudentStatus.fromValue(dbData);
        }
    }
}
