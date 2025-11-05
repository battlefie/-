package com.studyabroad.config;

import com.studyabroad.security.JwtAuthenticationFilter;
import com.studyabroad.security.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, 
                         CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/reset-password").permitAll()
                .requestMatchers("/api/auth/register").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/favicon.ico", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login.html", "/register.html", "/forgot-password.html", "/index.html", "/").permitAll()
                .requestMatchers("/dashboard.html", "/students.html", "/universities.html", "/applications.html",
                                 "/consultation-clients.html", "/advanced-search.html").permitAll()
                // 管理员可以访问所有API
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                // 大学管理：管理员、咨询顾问和文案可访问
                .requestMatchers("/api/universities/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_COUNSELOR", "ROLE_WRITER")
                // 申请管理：管理员和文案可访问
                .requestMatchers("/api/applications/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_WRITER")
                // 学生相关API：管理员可以访问所有，咨询顾问和文案只能访问自己的学生
                .requestMatchers("/api/students/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_COUNSELOR", "ROLE_WRITER")
                // 用户相关API：所有已认证用户可访问
                .requestMatchers("/api/users/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_COUNSELOR", "ROLE_WRITER")
                // 咨询客户管理：管理员和咨询顾问可访问
                .requestMatchers("/api/consultation-clients/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_COUNSELOR")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(customAuthenticationEntryPoint)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

