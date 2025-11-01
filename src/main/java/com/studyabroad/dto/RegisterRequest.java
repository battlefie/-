package com.studyabroad.dto;

import com.studyabroad.entity.User;
import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String realName;
    private String phone;
    private User.UserRole role = User.UserRole.WRITER;
}

