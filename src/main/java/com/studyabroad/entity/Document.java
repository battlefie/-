package com.studyabroad.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 申请材料实体类
 */
@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentType documentType;

    @Column(nullable = false, length = 200)
    private String fileName;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @PrePersist
    protected void onCreate() {
        uploadTime = LocalDateTime.now();
    }

    /**
     * 文档类型枚举
     */
    public enum DocumentType {
        TRANSCRIPT,         // 成绩单
        DIPLOMA,           // 毕业证
        DEGREE,            // 学位证
        LANGUAGE_TEST,     // 语言成绩
        RECOMMENDATION,    // 推荐信
        PERSONAL_STATEMENT, // 个人陈述
        CV,                // 简历
        PASSPORT,          // 护照
        OTHER              // 其他
    }
}

