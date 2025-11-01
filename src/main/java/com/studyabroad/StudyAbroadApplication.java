package com.studyabroad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 留学信息管理系统主启动类
 */
@SpringBootApplication
@EnableJpaAuditing
public class StudyAbroadApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyAbroadApplication.class, args);
    }
}

