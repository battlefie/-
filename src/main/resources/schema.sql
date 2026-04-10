-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    real_name VARCHAR(100) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    role ENUM('SUPER_ADMIN', 'ADMIN', 'COUNSELOR', 'WRITER') NOT NULL DEFAULT 'WRITER' COMMENT '角色',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 学生表
CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '学生姓名',
    status VARCHAR(20) DEFAULT '在途' COMMENT '学生状态',
    gender VARCHAR(10) COMMENT '性别',
    birth_date DATE COMMENT '出生日期',
    id_card VARCHAR(20) COMMENT '身份证号',
    address VARCHAR(500) COMMENT '地址',
    contact_info VARCHAR(500) COMMENT '联系方式（合并phone/email/wechat）',
    current_school VARCHAR(200) COMMENT '现就读学校',
    channel_source VARCHAR(100) COMMENT '渠道来源',
    intended_country VARCHAR(100) COMMENT '意向国家',
    enrolled_country VARCHAR(100) COMMENT '入读国家',
    enrolled_school VARCHAR(200) COMMENT '入读院校',
    contract_date DATE COMMENT '签约时间',
    contract_amount DECIMAL(12,2) COMMENT '签约金额',
    major VARCHAR(100) COMMENT '专业',
    gpa DECIMAL(3,2) COMMENT 'GPA',
    language_scores VARCHAR(200) COMMENT '语言/标化成绩',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=1 COMMENT='学生表';

-- 申请表
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    university_name VARCHAR(200) NOT NULL COMMENT '大学名称',
    university_email VARCHAR(200) COMMENT '申请学校邮箱',
    university_email_password VARCHAR(200) COMMENT '申请学校邮箱密码',
    country VARCHAR(100) COMMENT '国家',
    major VARCHAR(100) COMMENT '申请专业',
    degree_type ENUM('JUNIOR_HIGH', 'HIGH_SCHOOL', 'BACHELOR', 'MASTER', 'PHD') COMMENT '学位类型',
    status ENUM('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'ACCEPTED', 'REJECTED', 'WAITLISTED', 'WITHDRAWN') DEFAULT 'DRAFT' COMMENT '申请状态',
    application_date DATE COMMENT '申请日期',
    visa_submission_date DATE COMMENT '递交签证日期',
    interview_date DATE COMMENT '面试日期',
    fingerprint_collection_date DATE COMMENT '指纹采集日期',
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
    writer_id BIGINT COMMENT '负责文案ID（文案管理申请）',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 外键约束
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (writer_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='申请表';

-- 文档表（存放申请上传的文件）
CREATE TABLE IF NOT EXISTS documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL COMMENT '申请ID',
    file_path VARCHAR(500) COMMENT '上传文件路径（文件系统存储时使用）',
    file_name VARCHAR(255) COMMENT '上传文件名称',
    file_content LONGBLOB COMMENT '文件内容（数据库存储）',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_content_type VARCHAR(100) COMMENT '文件MIME类型',
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传日期',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表（存放申请上传的文件）';

-- 家庭信息表
CREATE TABLE IF NOT EXISTS family_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    -- 父亲信息
    father_name VARCHAR(100) COMMENT '父亲姓名',
    father_birth_date DATE COMMENT '父亲生日',
    father_contact VARCHAR(50) COMMENT '父亲联系方式',
    father_work_info VARCHAR(200) COMMENT '父亲工作单位及职务',
    father_education VARCHAR(100) COMMENT '父亲教育背景',
    father_income DECIMAL(15,2) COMMENT '父亲年收入（元）',
    -- 母亲信息
    mother_name VARCHAR(100) COMMENT '母亲姓名',
    mother_birth_date DATE COMMENT '母亲生日',
    mother_contact VARCHAR(50) COMMENT '母亲联系方式',
    mother_work_info VARCHAR(200) COMMENT '母亲工作单位及职务',
    mother_education VARCHAR(100) COMMENT '母亲教育背景',
    mother_income DECIMAL(15,2) COMMENT '母亲年收入（元）',
    -- 家庭资产信息
    annual_income DECIMAL(15,2) COMMENT '年收入（元）',
    real_estate_value DECIMAL(15,2) COMMENT '房产价值（元）',
    car_value DECIMAL(15,2) COMMENT '汽车价值（元）',
    stock_value DECIMAL(15,2) COMMENT '股票价值（元）',
    fund_value DECIMAL(15,2) COMMENT '基金价值（元）',
    deposit_value DECIMAL(15,2) COMMENT '存款价值（元）',
    other_investment_value DECIMAL(15,2) COMMENT '其他投资价值（元）',
    total_assets DECIMAL(15,2) COMMENT '总资产（元，计算字段）',
    -- 兄弟姐妹信息
    siblings_info TEXT COMMENT '兄弟姐妹信息',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭信息表';

-- 咨询客户表（独立表，不与任何表关联）
-- 字段与students表保持一致，便于转换为签约客户
CREATE TABLE IF NOT EXISTS consultation_clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    student_source VARCHAR(20) COMMENT '学生来源',
    status ENUM('潜在客户', '意向客户', '签约客户', '已流失') DEFAULT '潜在客户' COMMENT '客户状态',
    gender VARCHAR(10) COMMENT '性别',
    birth_date DATE COMMENT '出生日期',
    nationality VARCHAR(50) COMMENT '国籍',
    id_card VARCHAR(20) COMMENT '身份证号',
    address VARCHAR(500) COMMENT '地址',
    contact_info VARCHAR(500) COMMENT '联系方式（合并phone/email/wechat）',
    current_school VARCHAR(200) COMMENT '现就读学校',
    enrollment_date DATE COMMENT '入学时间',
    channel_source VARCHAR(100) COMMENT '渠道来源',
    intended_country VARCHAR(100) COMMENT '意向国家',
    major VARCHAR(100) COMMENT '专业',
    gpa DECIMAL(3,2) COMMENT 'GPA',
    language_scores VARCHAR(200) COMMENT '语言/标化成绩',
    awards TEXT COMMENT '获奖情况',
    experiences TEXT COMMENT '实习经历',
    notes TEXT COMMENT '备注',
    consultation_date DATE COMMENT '咨询日期',
    follow_up_status TEXT COMMENT '回访情况',
    counselor_id BIGINT COMMENT '负责咨询顾问ID',
    writer_id BIGINT COMMENT '负责文案ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_consultation_clients_counselor FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_consultation_clients_writer FOREIGN KEY (writer_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='咨询客户表';