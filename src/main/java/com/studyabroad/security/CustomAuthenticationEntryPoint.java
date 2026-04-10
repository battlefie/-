package com.studyabroad.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyabroad.dto.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 自定义认证入口点
 * 当用户未登录访问受保护资源时，返回401状态码
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        // 检查请求路径是否为HTML页面
        String requestPath = request.getRequestURI();
        boolean isHtmlRequest = requestPath.endsWith(".html") || 
                               requestPath.equals("/") || 
                               requestPath.equals("/dashboard") ||
                               requestPath.equals("/students") ||
                               requestPath.equals("/applications") ||
                               requestPath.equals("/consultation-clients") ||
                               requestPath.equals("/advanced-search") ||
                               requestPath.equals("/conversion-rate");
        
        if (isHtmlRequest) {
            // 对于HTML页面请求，返回401状态码，让前端JavaScript处理跳转
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            
            ApiResponse<Object> apiResponse = ApiResponse.error("未登录，请先登录");
            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(apiResponse);
            response.getWriter().write(jsonResponse);
        } else {
            // 对于API请求，返回401状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            
            ApiResponse<Object> apiResponse = ApiResponse.error("未授权访问");
            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(apiResponse);
            response.getWriter().write(jsonResponse);
        }
    }
}



