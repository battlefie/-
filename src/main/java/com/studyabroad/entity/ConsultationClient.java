package com.studyabroad.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 咨询客户实体类
 * 字段与Student实体保持一致，便于转换为签约客户
 */
@Entity
@Table(name = "consultation_clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ConsultationClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "student_source", length = 20)
    private String studentSourceStr;

    @Enumerated(EnumType.STRING)
    @Column
    private ClientStatus status;

    @Column(name = "gender", length = 10)
    private String genderStr;

    public Student.Gender getGender() {
        if (genderStr != null) {
            return Student.Gender.fromValue(genderStr);
        }
        return null;
    }

    public void setGender(Student.Gender gender) {
        this.genderStr = gender != null ? gender.getValue() : null;
    }

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 50)
    private String nationality;

    @Column(name = "id_card", length = 20)
    private String idCard;

    @Column(length = 500)
    private String address;

    @Column(name = "contact_info", length = 500)
    private String contactInfo;

    @Column(name = "current_school", length = 200)
    private String currentSchool;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    @Column(name = "channel_source", length = 100)
    private String channelSource;

    @Column(name = "intended_country", length = 100)
    private String intendedCountry;

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

    @Column(name = "consultation_date")
    private LocalDate consultationDate;

    @Column(name = "follow_up_status", columnDefinition = "TEXT")
    private String followUpStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "counselor_id", foreignKey = @ForeignKey(name = "fk_consultation_clients_counselor"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "email", "phone", "enabled", "createTime", "updateTime"})
    private com.studyabroad.entity.User counselor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "writer_id", foreignKey = @ForeignKey(name = "fk_consultation_clients_writer"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "email", "phone", "enabled", "createTime", "updateTime"})
    private com.studyabroad.entity.User writer;

    // 兼容性方法：获取counselorId
    public Long getCounselorId() {
        return counselor != null ? counselor.getId() : null;
    }

    public void setCounselorId(Long counselorId) {
        if (counselorId != null) {
            this.counselor = new com.studyabroad.entity.User();
            this.counselor.setId(counselorId);
        } else {
            this.counselor = null;
        }
    }

    // 兼容性方法：获取writerId
    public Long getWriterId() {
        return writer != null ? writer.getId() : null;
    }

    public void setWriterId(Long writerId) {
        if (writerId != null) {
            this.writer = new com.studyabroad.entity.User();
            this.writer.setId(writerId);
        } else {
            this.writer = null;
        }
    }

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // 兼容旧字段的getter/setter（用于向后兼容）
    @Transient
    public String getPhone() {
        if (contactInfo != null) {
            String[] parts = contactInfo.split(" / ");
            for (String part : parts) {
                if (part.matches("^[\\d\\s\\-+()]+$")) {
                    return part.trim();
                }
            }
        }
        return null;
    }

    @Transient
    public void setPhone(String phone) {
        // 如果contactInfo为空，则设置；否则合并
        if (contactInfo == null || contactInfo.isEmpty()) {
            contactInfo = phone;
        } else if (phone != null && !phone.isEmpty()) {
            contactInfo = phone + " / " + contactInfo;
        }
    }

    @Transient
    public String getEmail() {
        if (contactInfo != null) {
            String[] parts = contactInfo.split(" / ");
            for (String part : parts) {
                if (part.contains("@")) {
                    return part.trim();
                }
            }
        }
        return null;
    }

    @Transient
    public void setEmail(String email) {
        if (email != null && !email.isEmpty()) {
            if (contactInfo == null || contactInfo.isEmpty()) {
                contactInfo = email;
            } else {
                contactInfo = contactInfo + " / " + email;
            }
        }
    }

    @Transient
    public String getWechat() {
        if (contactInfo != null) {
            String[] parts = contactInfo.split(" / ");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.matches("^[\\d\\s\\-+()]+$") && !trimmed.contains("@")) {
                    return trimmed;
                }
            }
        }
        return null;
    }

    @Transient
    public void setWechat(String wechat) {
        if (wechat != null && !wechat.isEmpty()) {
            if (contactInfo == null || contactInfo.isEmpty()) {
                contactInfo = wechat;
            } else {
                contactInfo = contactInfo + " / " + wechat;
            }
        }
    }

    @Transient
    public String getContactPhone() {
        return getPhone();
    }

    @Transient
    public void setContactPhone(String contactPhone) {
        setPhone(contactPhone);
    }

    @Transient
    public String getChannel() {
        return channelSource;
    }

    @Transient
    public void setChannel(String channel) {
        this.channelSource = channel;
    }

    @Transient
    public String getTargetCountry() {
        return intendedCountry;
    }

    @Transient
    public void setTargetCountry(String targetCountry) {
        this.intendedCountry = targetCountry;
    }

    @Transient
    public String getEnglishScore() {
        return languageScores;
    }

    @Transient
    public void setEnglishScore(String englishScore) {
        this.languageScores = englishScore;
    }

    @Transient
    public String getHomeAddress() {
        return address;
    }

    @Transient
    public void setHomeAddress(String homeAddress) {
        this.address = homeAddress;
    }

    @Transient
    public TargetDegree getTargetDegree() {
        // 这个字段在Student中没有对应，保留用于咨询阶段
        return null;
    }

    @Transient
    public void setTargetDegree(TargetDegree targetDegree) {
        // 这个字段在Student中没有对应，保留用于咨询阶段
    }

    @Transient
    public LocalDate getGraduationDate() {
        // 这个字段在Student中没有对应，保留用于咨询阶段
        return null;
    }

    @Transient
    public void setGraduationDate(LocalDate graduationDate) {
        // 这个字段在Student中没有对应，保留用于咨询阶段
    }

    // StudentSource转换方法
    public Student.StudentSource getStudentSource() {
        if (studentSourceStr != null) {
            return Student.StudentSource.fromValue(studentSourceStr);
        }
        return null;
    }

    public void setStudentSource(Student.StudentSource studentSource) {
        this.studentSourceStr = studentSource != null ? studentSource.getValue() : null;
    }

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
     * 意向学位级别枚举
     */
    public enum TargetDegree {
        JUNIOR_HIGH, HIGH_SCHOOL, BACHELOR, MASTER, PHD
    }
}
