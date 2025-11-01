package com.studyabroad.dto;

import com.studyabroad.entity.Application;
import lombok.Data;
import java.time.LocalDate;

/**
 * 创建申请请求DTO
 */
@Data
public class CreateApplicationRequest {
    private Long studentId;
    private String universityName;
    private String country;
    private String major;
    private Application.DegreeType degreeType;
    private Application.ApplicationStatus status;
    private LocalDate applicationDate;
    private LocalDate visaSubmissionDate;
    private LocalDate interviewDate;
    private LocalDate medicalExamDate;
    private LocalDate visaApprovedDate;
    private LocalDate visaRejectedDate;
    private LocalDate departureDate;
    private String airportPickupAccommodation;
    private String followUpStatus;
    private String arrivalStatus;
    private String statusUrl;
    private String notes;
    private Long counselorId;
    private Long writerId;
}
