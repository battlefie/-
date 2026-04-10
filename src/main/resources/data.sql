-- 留学信息管理系统初始数据

USE study_abroad_db;

-- 插入管理员用户 (密码: admin123)
INSERT INTO users (username, password, email, real_name, phone, role, enabled, create_time, update_time)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@studyabroad.com', '系统管理员', '13800138000', 'SUPER_ADMIN', TRUE, NOW(), NOW());

-- 插入普通管理员（老板、文案主管），密码同样为 admin123
INSERT INTO users (username, password, email, real_name, phone, role, enabled, create_time, update_time)
VALUES 
('boss', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'boss@studyabroad.com', '老板', '13800138001', 'ADMIN', TRUE, NOW(), NOW()),
('copylead', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'copylead@studyabroad.com', '文案主管', '13800138002', 'ADMIN', TRUE, NOW(), NOW());

-- 插入咨询顾问用户 (密码: counselor123)
INSERT INTO users (username, password, email, real_name, phone, role, enabled, create_time, update_time)
VALUES ('counselor1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'counselor@studyabroad.com', '张顾问', '13900139000', 'COUNSELOR', TRUE, NOW(), NOW());

-- 插入文案用户 (密码: writer123)
INSERT INTO users (username, password, email, real_name, phone, role, enabled, create_time, update_time)
VALUES 
('writer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'writer1@example.com', '张文案', '13700137001', 'WRITER', TRUE, NOW(), NOW()),
('writer2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'writer2@example.com', '李文案', '13700137002', 'WRITER', TRUE, NOW(), NOW());

-- 插入学生详细信息（学生不再是用户，而是由咨询顾问管理的实体）
INSERT INTO students (name, student_source, status, gender, birth_date, nationality, id_card, address, phone, email, wechat, current_school, enrollment_date, channel_source, intended_country, enrolled_country, enrolled_school, contract_date, contract_amount, major, gpa, language_scores, awards, experiences, notes, counselor_id, writer_id, create_time, update_time)
VALUES 
('李明', '社会生源', '在途', '男', '2000-05-15', '中国', NULL, NULL, '13700137001', 'liming@example.com', 'liming_wx123', '北京大学', '2020-09-01', '网络推广', '美国', NULL, NULL, '2024-01-15', 50000.00, '计算机科学', 3.8, '托福 105', NULL, NULL, NULL, 4, 5, NOW(), NOW()),
('王芳', '四中国际部', '在途', '女', '2001-08-20', '中国', NULL, NULL, '13700137002', 'wangfang@example.com', 'wangfang_wx456', '清华大学', '2021-09-01', '朋友推荐', '英国', '英国', '牛津大学', '2024-02-20', 48000.00, '电子工程', 3.9, '雅思 7.5', NULL, NULL, NULL, 4, 6, NOW(), NOW());


-- 插入申请记录
INSERT INTO applications (student_id, university_name, country, major, degree_type, status, application_date, visa_submission_date, interview_date, medical_exam_date, visa_approved_date, visa_rejected_date, departure_date, airport_pickup_accommodation, follow_up_status, arrival_status, status_url, counselor_id, writer_id, create_time, update_time)
VALUES 
(1, '麻省理工学院', '美国', '计算机科学', 'MASTER', 'SUBMITTED', '2024-10-15', '2024-11-15', '2024-12-01', '2024-11-20', NULL, NULL, NULL, '已安排接机，住宿：学校宿舍', '待回访', '待抵达', 'https://apply.mit.edu/status/123456', 4, 5, NOW(), NOW()),
(1, '斯坦福大学', '美国', '人工智能', 'MASTER', 'UNDER_REVIEW', '2024-10-20', '2024-11-20', '2024-12-05', '2024-11-25', NULL, NULL, NULL, '已安排接机，住宿：校外公寓', '待回访', '待抵达', 'https://apply.stanford.edu/status/789012', 4, 5, NOW(), NOW()),
(2, '牛津大学', '英国', '电子工程', 'MASTER', 'ACCEPTED', '2024-09-10', '2024-10-10', '2024-10-25', '2024-10-15', '2024-11-01', NULL, '2025-01-15', '已安排接机，住宿：学校宿舍', '已回访，学生满意', '已抵达，适应良好', 'https://apply.ox.ac.uk/status/345678', 4, 6, NOW(), NOW()),
(2, '剑桥大学', '英国', '通信工程', 'MASTER', 'WAITLISTED', '2024-09-15', '2024-10-15', '2024-10-30', '2024-10-20', NULL, NULL, NULL, '待安排接机住宿', '待回访', '待抵达', 'https://apply.cam.ac.uk/status/901234', 4, 6, NOW(), NOW());

-- 插入咨询客户数据
INSERT INTO consultation_clients (name, contact_phone, status, consultation_date, gender, channel, target_country, target_degree, graduation_date, english_score, current_school, major, home_address, email, notes, follow_up_status, create_time, update_time)
VALUES 
('张小明', '13800138001', '潜在客户', '2024-12-01', '男', '网络推广', '美国', 'MASTER', '2025-06-01', 'TOEFL 95', '北京理工大学', '计算机科学', '北京市海淀区中关村大街1号', 'zhangxiaoming@example.com', '对计算机科学专业很感兴趣，希望申请美国名校', '已初步沟通，等待进一步联系', NOW(), NOW()),
('李小红', '13800138002', '意向客户', '2024-11-28', '女', '朋友推荐', '英国', 'BACHELOR', '2024-06-01', 'IELTS 6.5', '上海交通大学', '商科', '上海市浦东新区陆家嘴环路1000号', 'lixiaohong@example.com', '希望申请英国商科专业，对伦敦地区比较感兴趣', '已提供初步方案，客户比较满意', NOW(), NOW()),
('王大力', '13800138003', '签约客户', '2024-11-15', '男', '展会', '加拿大', 'MASTER', '2025-06-01', 'TOEFL 105', '清华大学', '工程学', '广州市天河区珠江新城花城大道85号', 'wangdali@example.com', '已签约，准备申请加拿大工程学硕士', '已签约，正在准备申请材料', NOW(), NOW()),
('赵小芳', '13800138004', '已流失', '2024-10-20', '女', '广告', '澳大利亚', 'BACHELOR', '2024-06-01', 'IELTS 6.0', '中山大学', '医学', '深圳市南山区科技园南区深南大道10000号', 'zhaoxiaofang@example.com', '原本考虑澳大利亚医学专业，后因个人原因放弃', '客户已明确表示不继续，已流失', NOW(), NOW());

-- 插入家庭信息数据
INSERT INTO family_info (student_id, father_name, father_birth_date, father_contact, father_work_info, father_education, mother_name, mother_birth_date, mother_contact, mother_work_info, mother_education, annual_income, real_estate_value, car_value, stock_value, fund_value, deposit_value, other_investment_value, total_assets, siblings_info, create_time, update_time)
VALUES 
(1, '李建国', '1970-03-15', '13800138001', '华为技术有限公司 · 软件工程师', '硕士', '王美丽', '1972-08-20', '13800138002', '中国银行 · 会计师', '本科', 450000.00, 1200000.00, 250000.00, 300000.00, 200000.00, 500000.00, 100000.00, 2550000.00, '妹妹：李晓晓，2005年生，高中在读', NOW(), NOW()),
(2, '王建军', '1968-11-10', '13800138004', '招商银行 · 金融分析师', '硕士', '李美丽', '1970-05-25', '13800138005', '北京大学附属中学 · 教师', '本科', 380000.00, 800000.00, 200000.00, 250000.00, 150000.00, 400000.00, 80000.00, 1880000.00, '哥哥：王强，1995年生，英国读研究生', NOW(), NOW());

