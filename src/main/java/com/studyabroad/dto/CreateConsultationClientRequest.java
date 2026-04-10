package com.studyabroad.dto;

import com.studyabroad.entity.ConsultationClient;
import com.studyabroad.entity.Student;
import lombok.Data;
import java.time.LocalDate;

/**
 * 创建咨询客户请求DTO
 * 字段与Student保持一致
 */
@Data
public class CreateConsultationClientRequest {
    private String name;
    private ConsultationClient.ClientStatus status;
    private Student.Gender gender;
    private LocalDate birthDate;
    private String idCard;
    private String address;
    private String contactInfo;
    private String currentSchool;
    private LocalDate enrollmentDate;
    private String channelSource;
    private String intendedCountry;
    private String major;
    private Double gpa;
    private String languageScores;
    private String awards;
    private String experiences;
    private String notes;
    private LocalDate consultationDate;
    private String followUpStatus;
    private Long counselorId;
    private Long writerId;
    
    // 兼容旧字段（用于向后兼容）
    @Deprecated
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
    
    @Deprecated
    public void setPhone(String phone) {
        if (contactInfo == null || contactInfo.isEmpty()) {
            contactInfo = phone;
        } else if (phone != null && !phone.isEmpty()) {
            contactInfo = phone + " / " + contactInfo;
        }
    }

    @Deprecated
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

    @Deprecated
    public void setEmail(String email) {
        if (email != null && !email.isEmpty()) {
            if (contactInfo == null || contactInfo.isEmpty()) {
                contactInfo = email;
            } else {
                contactInfo = contactInfo + " / " + email;
            }
        }
    }

    @Deprecated
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

    @Deprecated
    public void setWechat(String wechat) {
        if (wechat != null && !wechat.isEmpty()) {
            if (contactInfo == null || contactInfo.isEmpty()) {
                contactInfo = wechat;
            } else {
                contactInfo = contactInfo + " / " + wechat;
            }
        }
    }

    @Deprecated
    public String getContactPhone() {
        return getPhone();
    }
    
    @Deprecated
    public void setContactPhone(String contactPhone) {
        setPhone(contactPhone);
    }
    
    @Deprecated
    public String getChannel() {
        return channelSource;
    }
    
    @Deprecated
    public void setChannel(String channel) {
        this.channelSource = channel;
    }
    
    @Deprecated
    public String getTargetCountry() {
        return intendedCountry;
    }
    
    @Deprecated
    public void setTargetCountry(String targetCountry) {
        this.intendedCountry = targetCountry;
    }
    
    @Deprecated
    public String getEnglishScore() {
        return languageScores;
    }
    
    @Deprecated
    public void setEnglishScore(String englishScore) {
        this.languageScores = englishScore;
    }
    
    @Deprecated
    public String getHomeAddress() {
        return address;
    }
    
    @Deprecated
    public void setHomeAddress(String homeAddress) {
        this.address = homeAddress;
    }
}
