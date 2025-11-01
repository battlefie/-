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
    private Student.StudentSource studentSource;
    private Student.StudentStatus status;
    private Student.Gender gender;
    private LocalDate birthDate;
    private String nationality;
    private String idCard;
    private String address;
    private String phone;
    private String email;
    private String wechat;
    private String currentSchool;
    private LocalDate enrollmentDate;
    private String channelSource;
    private LocalDate contractDate;
    private Double contractAmount;
    private String major;
    private Double gpa;
    private Integer toeflScore;
    private Double ieltsScore;
    private Integer greScore;
    private Integer gmatScore;
    private String awards;
    private String experiences;
    private String notes;
    private Long counselorId;
    private Long writerId;
}
