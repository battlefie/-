package com.studyabroad.dto;

import com.studyabroad.entity.ConsultationClient;
import lombok.Data;
import java.time.LocalDate;

/**
 * 创建咨询客户请求DTO
 */
@Data
public class CreateConsultationClientRequest {
    private String name;
    private String contactPhone;
    private ConsultationClient.ClientStatus status;
    private LocalDate consultationDate;
    private ConsultationClient.Gender gender;
    private String channel;
    private String targetCountry;
    private ConsultationClient.TargetDegree targetDegree;
    private LocalDate graduationDate;
    private String englishScore;
    private String currentSchool;
    private String major;
    private String homeAddress;
    private String email;
    private String notes;
    private String followUpStatus;
}
