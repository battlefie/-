package com.studyabroad.controller;

import com.studyabroad.dto.ApiResponse;
import com.studyabroad.entity.University;
import com.studyabroad.service.UniversityService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

/**
 * 大学控制器
 */
@RestController
@RequestMapping("/api/universities")
public class UniversityController {

    private final UniversityService universityService;

    public UniversityController(UniversityService universityService) {
        this.universityService = universityService;
    }

    @PostMapping
    public ApiResponse<University> createUniversity(@RequestBody University university, Authentication authentication) {
        try {
            // 检查用户权限，管理员、咨询顾问和文案可以创建大学
            if (!hasAdminOrCounselorOrWriterRole(authentication)) {
                return ApiResponse.error("权限不足，只有管理员、咨询顾问和文案可以创建大学信息");
            }
            
            University createdUniversity = universityService.createUniversity(university);
            return ApiResponse.success("大学信息创建成功", createdUniversity);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<University> getUniversityById(@PathVariable Long id) {
        try {
            University university = universityService.getUniversityById(id);
            return ApiResponse.success(university);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<?> getAllUniversities(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            // 如果提供了分页参数，返回分页数据
            if (page != null && size != null) {
                return ApiResponse.success(universityService.getUniversities(page, size));
            }
            
            // 否则返回列表数据（保持向后兼容）
            List<University> universities;
            if (country != null) {
                universities = universityService.getUniversitiesByCountry(country);
            } else if (name != null) {
                universities = universityService.searchUniversitiesByName(name);
            } else {
                universities = universityService.getAllUniversities();
            }
            return ApiResponse.success(universities);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<University> updateUniversity(@PathVariable Long id, @RequestBody University university, Authentication authentication) {
        try {
            // 检查用户权限，管理员、咨询顾问和文案可以更新大学
            if (!hasAdminOrCounselorOrWriterRole(authentication)) {
                return ApiResponse.error("权限不足，只有管理员、咨询顾问和文案可以更新大学信息");
            }
            
            University updatedUniversity = universityService.updateUniversity(id, university);
            return ApiResponse.success("大学信息更新成功", updatedUniversity);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUniversity(@PathVariable Long id, Authentication authentication) {
        try {
            // 检查用户权限，只有管理员可以删除大学
            if (!hasAdminRole(authentication)) {
                return ApiResponse.error("权限不足，只有管理员可以删除大学信息");
            }
            
            universityService.deleteUniversity(id);
            return ApiResponse.success("大学信息删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
        /**
         * 检查当前用户是否有管理员权限
         */
        private boolean hasAdminRole(Authentication authentication) {
            if (authentication == null) {
                return false;
            }
            
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            return authorities.stream()
                    .anyMatch(authority -> "ADMIN".equals(authority.getAuthority()) || 
                                           "ROLE_ADMIN".equals(authority.getAuthority()));
        }
        
        /**
         * 检查当前用户是否有管理员或咨询顾问权限
         */
        private boolean hasAdminOrCounselorRole(Authentication authentication) {
            if (authentication == null) {
                return false;
            }
            
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            return authorities.stream()
                    .anyMatch(authority -> 
                        "ADMIN".equals(authority.getAuthority()) || 
                        "ROLE_ADMIN".equals(authority.getAuthority()) ||
                        "COUNSELOR".equals(authority.getAuthority()) ||
                        "ROLE_COUNSELOR".equals(authority.getAuthority())
                    );
        }
        
        /**
         * 检查当前用户是否有管理员、咨询顾问或文案权限
         */
        private boolean hasAdminOrCounselorOrWriterRole(Authentication authentication) {
            if (authentication == null) {
                return false;
            }
            
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            return authorities.stream()
                    .anyMatch(authority -> 
                        "ADMIN".equals(authority.getAuthority()) || 
                        "ROLE_ADMIN".equals(authority.getAuthority()) ||
                        "COUNSELOR".equals(authority.getAuthority()) ||
                        "ROLE_COUNSELOR".equals(authority.getAuthority()) ||
                        "WRITER".equals(authority.getAuthority()) ||
                        "ROLE_WRITER".equals(authority.getAuthority())
                    );
        }
}

