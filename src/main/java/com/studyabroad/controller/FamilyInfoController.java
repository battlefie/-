package com.studyabroad.controller;

import com.studyabroad.dto.ApiResponse;
import com.studyabroad.dto.CreateFamilyInfoRequest;
import com.studyabroad.entity.FamilyInfo;
import com.studyabroad.entity.Student;
import com.studyabroad.repository.StudentRepository;
import com.studyabroad.service.FamilyInfoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 家庭信息管理控制器
 */
@RestController
@RequestMapping("/api/family-info")
public class FamilyInfoController {

    private final FamilyInfoService familyInfoService;
    private final StudentRepository studentRepository;

    public FamilyInfoController(FamilyInfoService familyInfoService, StudentRepository studentRepository) {
        this.familyInfoService = familyInfoService;
        this.studentRepository = studentRepository;
    }

    /**
     * 获取所有家庭信息
     */
    @GetMapping
    public ApiResponse<List<FamilyInfo>> getAllFamilyInfo() {
        try {
            List<FamilyInfo> familyInfos = familyInfoService.getAllFamilyInfo();
            return ApiResponse.success("家庭信息列表获取成功", familyInfos);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取家庭信息
     */
    @GetMapping("/{id}")
    public ApiResponse<FamilyInfo> getFamilyInfoById(@PathVariable Long id) {
        try {
            return familyInfoService.getFamilyInfoById(id)
                    .map(familyInfo -> ApiResponse.success("家庭信息获取成功", familyInfo))
                    .orElse(ApiResponse.error("家庭信息未找到"));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据学生ID获取家庭信息
     */
    @GetMapping("/student/{studentId}")
    public ApiResponse<FamilyInfo> getFamilyInfoByStudentId(@PathVariable Long studentId) {
        try {
            return familyInfoService.getFamilyInfoByStudentId(studentId)
                    .map(familyInfo -> ApiResponse.success("家庭信息获取成功", familyInfo))
                    .orElse(ApiResponse.error("该学生无家庭信息"));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查学生是否存在家庭信息
     */
    @GetMapping("/exists/student/{studentId}")
    public ApiResponse<Boolean> existsFamilyInfoByStudentId(@PathVariable Long studentId) {
        try {
            boolean exists = familyInfoService.existsByStudentId(studentId);
            return ApiResponse.success("查询成功", exists);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建家庭信息
     */
    @PostMapping
    public ApiResponse<FamilyInfo> createFamilyInfo(@RequestBody CreateFamilyInfoRequest request) {
        try {
            System.out.println("接收到的家庭信息请求: " + request);
            
            // 确保学生ID存在
            if (request.getStudentId() == null) {
                return ApiResponse.error("请提供学生ID");
            }
            
            // 获取学生信息
            Optional<Student> studentOpt = studentRepository.findById(request.getStudentId());
            if (!studentOpt.isPresent()) {
                return ApiResponse.error("学生不存在，ID: " + request.getStudentId());
            }
            
            Student student = studentOpt.get();
            System.out.println("找到学生: " + student);
            
            // 创建FamilyInfo对象
            FamilyInfo familyInfo = new FamilyInfo();
            familyInfo.setStudent(student);
            
            // 设置父亲信息
            familyInfo.setFatherName(request.getFatherName());
            familyInfo.setFatherBirthDate(request.getFatherBirthDate());
            familyInfo.setFatherContact(request.getFatherContact());
            familyInfo.setFatherWorkInfo(request.getFatherWorkInfo());
            familyInfo.setFatherEducation(request.getFatherEducation());
            familyInfo.setFatherIncome(request.getFatherIncome());
            
            // 设置母亲信息
            familyInfo.setMotherName(request.getMotherName());
            familyInfo.setMotherBirthDate(request.getMotherBirthDate());
            familyInfo.setMotherContact(request.getMotherContact());
            familyInfo.setMotherWorkInfo(request.getMotherWorkInfo());
            familyInfo.setMotherEducation(request.getMotherEducation());
            familyInfo.setMotherIncome(request.getMotherIncome());
            
            // 设置资产信息
            familyInfo.setAnnualIncome(request.getAnnualIncome());
            familyInfo.setRealEstateValue(request.getRealEstateValue());
            familyInfo.setCarValue(request.getCarValue());
            familyInfo.setStockValue(request.getStockValue());
            familyInfo.setFundValue(request.getFundValue());
            familyInfo.setDepositValue(request.getDepositValue());
            familyInfo.setOtherInvestmentValue(request.getOtherInvestmentValue());
            
            // 兄弟姐妹信息
            familyInfo.setSiblingsInfo(request.getSiblingsInfo());
            
            FamilyInfo createdFamilyInfo = familyInfoService.createFamilyInfo(familyInfo);
            return ApiResponse.success("家庭信息创建成功", createdFamilyInfo);
        } catch (Exception e) {
            System.out.println("创建家庭信息异常: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新家庭信息
     */
    @PutMapping("/{id}")
    public ApiResponse<FamilyInfo> updateFamilyInfo(@PathVariable Long id, @RequestBody CreateFamilyInfoRequest request) {
        try {
            System.out.println("接收到的家庭信息更新请求，ID: " + id);
            System.out.println("接收到的数据: " + request);
            System.out.println("父亲收入: " + request.getFatherIncome());
            System.out.println("母亲收入: " + request.getMotherIncome());
            
            // 获取现有的家庭信息
            Optional<FamilyInfo> existingFamilyInfoOpt = familyInfoService.getFamilyInfoById(id);
            if (!existingFamilyInfoOpt.isPresent()) {
                return ApiResponse.error("家庭信息未找到，ID: " + id);
            }
            
            FamilyInfo familyInfo = existingFamilyInfoOpt.get();
            
            // 更新父亲信息
            if (request.getFatherName() != null) familyInfo.setFatherName(request.getFatherName());
            if (request.getFatherBirthDate() != null) familyInfo.setFatherBirthDate(request.getFatherBirthDate());
            if (request.getFatherContact() != null) familyInfo.setFatherContact(request.getFatherContact());
            if (request.getFatherWorkInfo() != null) familyInfo.setFatherWorkInfo(request.getFatherWorkInfo());
            if (request.getFatherEducation() != null) familyInfo.setFatherEducation(request.getFatherEducation());
            if (request.getFatherIncome() != null) familyInfo.setFatherIncome(request.getFatherIncome());
            
            // 更新母亲信息
            if (request.getMotherName() != null) familyInfo.setMotherName(request.getMotherName());
            if (request.getMotherBirthDate() != null) familyInfo.setMotherBirthDate(request.getMotherBirthDate());
            if (request.getMotherContact() != null) familyInfo.setMotherContact(request.getMotherContact());
            if (request.getMotherWorkInfo() != null) familyInfo.setMotherWorkInfo(request.getMotherWorkInfo());
            if (request.getMotherEducation() != null) familyInfo.setMotherEducation(request.getMotherEducation());
            if (request.getMotherIncome() != null) familyInfo.setMotherIncome(request.getMotherIncome());
            
            // 更新资产信息
            if (request.getAnnualIncome() != null) familyInfo.setAnnualIncome(request.getAnnualIncome());
            if (request.getRealEstateValue() != null) familyInfo.setRealEstateValue(request.getRealEstateValue());
            if (request.getCarValue() != null) familyInfo.setCarValue(request.getCarValue());
            if (request.getStockValue() != null) familyInfo.setStockValue(request.getStockValue());
            if (request.getFundValue() != null) familyInfo.setFundValue(request.getFundValue());
            if (request.getDepositValue() != null) familyInfo.setDepositValue(request.getDepositValue());
            if (request.getOtherInvestmentValue() != null) familyInfo.setOtherInvestmentValue(request.getOtherInvestmentValue());
            
            if (request.getSiblingsInfo() != null) familyInfo.setSiblingsInfo(request.getSiblingsInfo());
            
            FamilyInfo updatedFamilyInfo = familyInfoService.updateFamilyInfo(id, familyInfo);
            System.out.println("更新后的家庭信息: " + updatedFamilyInfo);
            System.out.println("更新后的父亲收入: " + updatedFamilyInfo.getFatherIncome());
            System.out.println("更新后的母亲收入: " + updatedFamilyInfo.getMotherIncome());
            
            return ApiResponse.success("家庭信息更新成功", updatedFamilyInfo);
        } catch (Exception e) {
            System.out.println("更新家庭信息异常: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除家庭信息
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFamilyInfo(@PathVariable Long id) {
        try {
            familyInfoService.deleteFamilyInfo(id);
            return ApiResponse.success("家庭信息删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
