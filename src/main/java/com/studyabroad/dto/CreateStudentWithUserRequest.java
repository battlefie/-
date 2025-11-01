package com.studyabroad.dto;

import com.studyabroad.entity.User;
import lombok.Data;
import java.time.LocalDate;

/**
 * 创建学生和用户请求DTO
 */
@Data
public class CreateStudentWithUserRequest {
    // 用户信息
    private String username;
    private String password;
    private String email;
    private String realName;
    private String phone;
    private User.UserRole role = User.UserRole.WRITER;
    
    // 学生信息
    private String gender;
    private LocalDate birthDate;
    private String nationality;
    private String idCard;
    private String address;
    private String highSchool;
    private String university;
    private String major;
    private Double gpa;
    private Integer toeflScore;
    private Double ieltsScore;
    private Integer greScore;
    private Integer gmatScore;
    private String awards;
    private String experiences;
    private String notes;
}