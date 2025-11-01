package com.studyabroad.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建大学请求DTO
 */
@Data
public class CreateUniversityRequest {
    private String name;
    private String country;
    private String city;
    private Integer shanghaiRanking;
    private Integer timesRanking;
    private Integer qsRanking;
    private Integer usNewsRanking;
    private BigDecimal tuitionFee;
    private String languageRequirement;
    private BigDecimal gpaRequirement;
    private LocalDate applicationDeadline;
    private String description;
    private String website;
}
