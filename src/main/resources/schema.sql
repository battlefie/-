-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    real_name VARCHAR(100) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    role ENUM('ADMIN', 'COUNSELOR', 'WRITER') NOT NULL DEFAULT 'WRITER' COMMENT '角色',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 学生表
CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '学生姓名',
    student_source VARCHAR(20) COMMENT '学生来源',
    status VARCHAR(20) DEFAULT '在途' COMMENT '学生状态',
    gender VARCHAR(10) COMMENT '性别',
    birth_date DATE COMMENT '出生日期',
    nationality VARCHAR(50) COMMENT '国籍',
    id_card VARCHAR(20) COMMENT '身份证号',
    address VARCHAR(500) COMMENT '地址',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    wechat VARCHAR(100) COMMENT '微信',
    current_school VARCHAR(200) COMMENT '现就读学校',
    enrollment_date DATE COMMENT '入学时间',
    channel_source VARCHAR(100) COMMENT '渠道来源',
    contract_date DATE COMMENT '签约时间',
    contract_amount DECIMAL(12,2) COMMENT '签约金额',
    major VARCHAR(100) COMMENT '专业',
    gpa DECIMAL(3,2) COMMENT 'GPA',
    toefl_score INT COMMENT '托福成绩',
    ielts_score DECIMAL(3,1) COMMENT '雅思成绩',
    gre_score INT COMMENT 'GRE成绩',
    gmat_score INT COMMENT 'GMAT成绩',
    awards TEXT COMMENT '获奖情况',
    experiences TEXT COMMENT '实习经历',
    notes TEXT COMMENT '备注',
    -- 权限控制字段
    counselor_id BIGINT COMMENT '负责咨询顾问ID',
    writer_id BIGINT COMMENT '负责文案ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 外键约束
    FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (writer_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

-- 大学表
CREATE TABLE IF NOT EXISTS universities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '大学名称',
    country VARCHAR(100) NOT NULL COMMENT '国家',
    city VARCHAR(100) COMMENT '城市',
    shanghai_ranking INT COMMENT '软科排名',
    times_ranking INT COMMENT '泰晤士排名',
    qs_ranking INT COMMENT 'QS排名',
    us_news_ranking INT COMMENT 'US News排名',
    tuition_fee DECIMAL(12,2) COMMENT '学费',
    language_requirement TEXT COMMENT '语言要求',
    gpa_requirement DECIMAL(3,2) COMMENT 'GPA要求',
    application_deadline DATE COMMENT '申请截止日期',
    description TEXT COMMENT '描述',
    website VARCHAR(500) COMMENT '官网',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大学表';

-- 申请表
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    university_name VARCHAR(200) NOT NULL COMMENT '大学名称',
    country VARCHAR(100) COMMENT '国家',
    major VARCHAR(100) COMMENT '申请专业',
    degree_type ENUM('JUNIOR_HIGH', 'HIGH_SCHOOL', 'BACHELOR', 'MASTER', 'PHD') COMMENT '学位类型',
    status ENUM('DRAFT', 'PENDING', 'SUBMITTED', 'UNDER_REVIEW', 'ACCEPTED', 'REJECTED', 'WAITLISTED', 'WITHDRAWN') DEFAULT 'DRAFT' COMMENT '申请状态',
    application_date DATE COMMENT '申请日期',
    visa_submission_date DATE COMMENT '递交签证日期',
    interview_date DATE COMMENT '面试日期',
    medical_exam_date DATE COMMENT '体检日期',
    visa_approved_date DATE COMMENT '获签日期',
    visa_rejected_date DATE COMMENT '拒签日期',
    departure_date DATE COMMENT '出发日期',
    airport_pickup_accommodation TEXT COMMENT '接机住宿',
    follow_up_status TEXT COMMENT '回访情况',
    arrival_status TEXT COMMENT '抵达后情况',
    status_url VARCHAR(500) COMMENT '申请状态链接（用于放网址）',
    notes TEXT COMMENT '备注',
    -- 权限控制字段
    counselor_id BIGINT COMMENT '负责咨询顾问ID',
    writer_id BIGINT NOT NULL COMMENT '负责文案ID（文案管理申请）',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 外键约束
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (writer_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='申请表';

-- 文档表
CREATE TABLE IF NOT EXISTS documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL COMMENT '申请ID',
    document_type ENUM('TRANSCRIPT', 'RECOMMENDATION_LETTER', 'PERSONAL_STATEMENT', 'CV', 'LANGUAGE_CERTIFICATE', 'OTHER') NOT NULL COMMENT '文档类型',
    document_name VARCHAR(200) NOT NULL COMMENT '文档名称',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传日期',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表';

-- 家庭信息表
CREATE TABLE IF NOT EXISTS family_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    -- 父亲信息
    father_name VARCHAR(100) COMMENT '父亲姓名',
    father_birth_date DATE COMMENT '父亲生日',
    father_phone VARCHAR(20) COMMENT '父亲手机号',
    father_occupation VARCHAR(100) COMMENT '父亲职业',
    father_company VARCHAR(200) COMMENT '父亲公司',
    -- 母亲信息
    mother_name VARCHAR(100) COMMENT '母亲姓名',
    mother_birth_date DATE COMMENT '母亲生日',
    mother_phone VARCHAR(20) COMMENT '母亲手机号',
    mother_occupation VARCHAR(100) COMMENT '母亲职业',
    mother_company VARCHAR(200) COMMENT '母亲公司',
    -- 家庭资产信息
    annual_income DECIMAL(15,2) COMMENT '年收入',
    real_estate_value DECIMAL(15,2) COMMENT '房产价值',
    car_value DECIMAL(15,2) COMMENT '汽车价值',
    stock_value DECIMAL(15,2) COMMENT '股票价值',
    fund_value DECIMAL(15,2) COMMENT '基金价值',
    deposit_value DECIMAL(15,2) COMMENT '存款价值',
    other_investment_value DECIMAL(15,2) COMMENT '其他投资价值',
    total_assets DECIMAL(15,2) COMMENT '总资产',
    -- 其他家庭信息
    family_address VARCHAR(500) COMMENT '家庭住址',
    family_size INT COMMENT '家庭人口数',
    emergency_contact_name VARCHAR(100) COMMENT '紧急联系人姓名',
    emergency_contact_phone VARCHAR(20) COMMENT '紧急联系人电话',
    emergency_contact_relation VARCHAR(50) COMMENT '紧急联系人关系',
    notes TEXT COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭信息表';

-- 咨询客户表（独立表，不与任何表关联）
CREATE TABLE IF NOT EXISTS consultation_clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    contact_phone VARCHAR(20) COMMENT '联系方式（手机号）',
    status ENUM('潜在客户', '意向客户', '签约客户', '已流失') DEFAULT '潜在客户' COMMENT '客户状态',
    consultation_date DATE COMMENT '咨询日期',
    gender ENUM('男', '女') COMMENT '性别',
    channel VARCHAR(100) COMMENT '渠道来源',
    target_country VARCHAR(100) COMMENT '意向国家',
    target_degree ENUM('JUNIOR_HIGH', 'HIGH_SCHOOL', 'BACHELOR', 'MASTER', 'PHD') COMMENT '意向学位级别',
    graduation_date DATE COMMENT '毕业时间',
    english_score VARCHAR(50) COMMENT '英语成绩（如：TOEFL 100, IELTS 7.0）',
    current_school VARCHAR(200) COMMENT '现就读学校',
    major VARCHAR(100) COMMENT '专业',
    home_address VARCHAR(500) COMMENT '家庭住址',
    email VARCHAR(100) COMMENT '邮箱地址',
    notes TEXT COMMENT '备注',
    follow_up_status TEXT COMMENT '回访情况',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='咨询客户表';