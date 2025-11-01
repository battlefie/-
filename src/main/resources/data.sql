-- 留学信息管理系统初始数据

USE study_abroad_db;

-- 插入管理员用户 (密码: admin123)
INSERT INTO users (username, password, email, real_name, phone, role, enabled, create_time, update_time)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@studyabroad.com', '系统管理员', '13800138000', 'ADMIN', TRUE, NOW(), NOW());

-- 插入咨询顾问用户 (密码: counselor123)
INSERT INTO users (username, password, email, real_name, phone, role, enabled, create_time, update_time)
VALUES ('counselor1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'counselor@studyabroad.com', '张顾问', '13900139000', 'COUNSELOR', TRUE, NOW(), NOW());

-- 插入文案用户 (密码: writer123)
INSERT INTO users (username, password, email, real_name, phone, role, enabled, create_time, update_time)
VALUES 
('writer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'writer1@example.com', '张文案', '13700137001', 'WRITER', TRUE, NOW(), NOW()),
('writer2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'writer2@example.com', '李文案', '13700137002', 'WRITER', TRUE, NOW(), NOW());

-- 插入学生详细信息（学生不再是用户，而是由咨询顾问管理的实体）
INSERT INTO students (name, student_source, status, gender, birth_date, nationality, id_card, address, phone, email, wechat, current_school, enrollment_date, channel_source, contract_date, contract_amount, major, gpa, toefl_score, ielts_score, gre_score, gmat_score, awards, experiences, notes, counselor_id, writer_id, create_time, update_time)
VALUES 
('李明', '社会生源', '在途', '男', '2000-05-15', '中国', NULL, NULL, '13700137001', 'liming@example.com', 'liming_wx123', '北京大学', '2020-09-01', '网络推广', '2024-01-15', 50000.00, '计算机科学', 3.8, 105, NULL, NULL, NULL, NULL, NULL, NULL, 2, 3, NOW(), NOW()),
('王芳', '四中国际部', '在途', '女', '2001-08-20', '中国', NULL, NULL, '13700137002', 'wangfang@example.com', 'wangfang_wx456', '清华大学', '2021-09-01', '朋友推荐', '2024-02-20', 48000.00, '电子工程', 3.9, NULL, 7.5, NULL, NULL, NULL, NULL, NULL, 2, 4, NOW(), NOW());

-- 插入大学信息
INSERT INTO universities (name, country, city, shanghai_ranking, times_ranking, qs_ranking, us_news_ranking, tuition_fee, language_requirement, gpa_requirement, application_deadline, description, website, create_time, update_time)
VALUES 
('麻省理工学院', '美国', '波士顿', 1, 2, 1, 2, 57786.00, 'TOEFL 100+, IELTS 7.0+', 3.70, '2024-12-15', 'MIT是世界顶尖的理工科大学', 'https://www.mit.edu', NOW(), NOW()),
('斯坦福大学', '美国', '帕洛阿尔托', 2, 4, 3, 3, 61731.00, 'TOEFL 100+, IELTS 7.0+', 3.70, '2024-12-01', '斯坦福大学位于硅谷核心', 'https://www.stanford.edu', NOW(), NOW()),
('哈佛大学', '美国', '剑桥', 3, 1, 4, 1, 57826.00, 'TOEFL 100+, IELTS 7.0+', 3.80, '2024-12-15', '哈佛大学是美国最古老的大学', 'https://www.harvard.edu', NOW(), NOW()),
('牛津大学', '英国', '牛津', 7, 1, 2, 5, 26770.00, 'IELTS 7.0+, TOEFL 110+', 3.70, '2025-01-15', '牛津大学是英语世界最古老的大学', 'https://www.ox.ac.uk', NOW(), NOW()),
('剑桥大学', '英国', '剑桥', 4, 3, 5, 8, 25734.00, 'IELTS 7.0+, TOEFL 110+', 3.70, '2025-01-15', '剑桥大学拥有800多年历史', 'https://www.cam.ac.uk', NOW(), NOW());


-- 插入申请记录
INSERT INTO applications (student_id, university_name, country, major, degree_type, status, application_date, visa_submission_date, interview_date, medical_exam_date, visa_approved_date, visa_rejected_date, departure_date, airport_pickup_accommodation, follow_up_status, arrival_status, status_url, counselor_id, writer_id, create_time, update_time)
VALUES 
(1, '麻省理工学院', '美国', '计算机科学', 'MASTER', 'SUBMITTED', '2024-10-15', '2024-11-15', '2024-12-01', '2024-11-20', NULL, NULL, NULL, '已安排接机，住宿：学校宿舍', '待回访', '待抵达', 'https://apply.mit.edu/status/123456', 2, 3, NOW(), NOW()),
(1, '斯坦福大学', '美国', '人工智能', 'MASTER', 'UNDER_REVIEW', '2024-10-20', '2024-11-20', '2024-12-05', '2024-11-25', NULL, NULL, NULL, '已安排接机，住宿：校外公寓', '待回访', '待抵达', 'https://apply.stanford.edu/status/789012', 2, 3, NOW(), NOW()),
(2, '牛津大学', '英国', '电子工程', 'MASTER', 'ACCEPTED', '2024-09-10', '2024-10-10', '2024-10-25', '2024-10-15', '2024-11-01', NULL, '2025-01-15', '已安排接机，住宿：学校宿舍', '已回访，学生满意', '已抵达，适应良好', 'https://apply.ox.ac.uk/status/345678', 2, 4, NOW(), NOW()),
(2, '剑桥大学', '英国', '通信工程', 'MASTER', 'WAITLISTED', '2024-09-15', '2024-10-15', '2024-10-30', '2024-10-20', NULL, NULL, NULL, '待安排接机住宿', '待回访', '待抵达', 'https://apply.cam.ac.uk/status/901234', 2, 4, NOW(), NOW());

-- 插入咨询客户数据
INSERT INTO consultation_clients (name, contact_phone, status, consultation_date, gender, channel, target_country, target_degree, graduation_date, english_score, current_school, major, home_address, email, notes, follow_up_status, create_time, update_time)
VALUES 
('张小明', '13800138001', '潜在客户', '2024-12-01', '男', '网络推广', '美国', 'MASTER', '2025-06-01', 'TOEFL 95', '北京理工大学', '计算机科学', '北京市海淀区中关村大街1号', 'zhangxiaoming@example.com', '对计算机科学专业很感兴趣，希望申请美国名校', '已初步沟通，等待进一步联系', NOW(), NOW()),
('李小红', '13800138002', '意向客户', '2024-11-28', '女', '朋友推荐', '英国', 'BACHELOR', '2024-06-01', 'IELTS 6.5', '上海交通大学', '商科', '上海市浦东新区陆家嘴环路1000号', 'lixiaohong@example.com', '希望申请英国商科专业，对伦敦地区比较感兴趣', '已提供初步方案，客户比较满意', NOW(), NOW()),
('王大力', '13800138003', '签约客户', '2024-11-15', '男', '展会', '加拿大', 'MASTER', '2025-06-01', 'TOEFL 105', '清华大学', '工程学', '广州市天河区珠江新城花城大道85号', 'wangdali@example.com', '已签约，准备申请加拿大工程学硕士', '已签约，正在准备申请材料', NOW(), NOW()),
('赵小芳', '13800138004', '已流失', '2024-10-20', '女', '广告', '澳大利亚', 'BACHELOR', '2024-06-01', 'IELTS 6.0', '中山大学', '医学', '深圳市南山区科技园南区深南大道10000号', 'zhaoxiaofang@example.com', '原本考虑澳大利亚医学专业，后因个人原因放弃', '客户已明确表示不继续，已流失', NOW(), NOW());

-- 插入家庭信息数据
INSERT INTO family_info (student_id, father_name, father_birth_date, father_phone, father_occupation, father_company, mother_name, mother_birth_date, mother_phone, mother_occupation, mother_company, annual_income, real_estate_value, car_value, stock_value, fund_value, deposit_value, other_investment_value, total_assets, family_address, family_size, emergency_contact_name, emergency_contact_phone, emergency_contact_relation, notes, create_time, update_time)
VALUES 
(1, '李建国', '1970-03-15', '13800138001', '软件工程师', '华为技术有限公司', '王美丽', '1972-08-20', '13800138002', '会计师', '中国银行', 450000.00, 1200000.00, 250000.00, 300000.00, 200000.00, 500000.00, 100000.00, 2550000.00, '北京市海淀区中关村大街1号', 3, '李奶奶', '13800138003', '祖母', '家庭经济状况良好，支持孩子出国留学', NOW(), NOW()),
(2, '王建军', '1968-11-10', '13800138004', '金融分析师', '招商银行', '李美丽', '1970-05-25', '13800138005', '教师', '北京大学附属中学', 380000.00, 800000.00, 200000.00, 250000.00, 150000.00, 400000.00, 80000.00, 1880000.00, '上海市浦东新区陆家嘴环路1000号', 3, '王爷爷', '13800138006', '祖父', '家庭收入稳定，教育背景良好', NOW(), NOW());

