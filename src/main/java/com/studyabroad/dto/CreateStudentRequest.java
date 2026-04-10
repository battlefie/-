package com.studyabroad.dto;

import com.studyabroad.entity.Student;
import lombok.Data;
import java.time.LocalDate;

/**
 * 创建学生请求DTO
 */
@Data
public class CreateStudentRequest {
    private String name;
    private Student.StudentStatus status;
    private Student.Gender gender;
    private LocalDate birthDate;
    private String idCard;
    private String address;
    private String contactInfo;
    private String currentSchool;
    private String channelSource;
    private String intendedCountry;
    private String enrolledCountry;
    private String enrolledSchool;
    private LocalDate contractDate;
    private Double contractAmount;
    private String major;
    private Double gpa;
    private String languageScores;
    private String awards;
    private String experiences;
    private String notes;
    private Long counselorId;
    private Long writerId;
}
