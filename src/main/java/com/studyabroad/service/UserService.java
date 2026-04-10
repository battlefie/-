package com.studyabroad.service;

import com.studyabroad.dto.LoginRequest;
import com.studyabroad.dto.RegisterRequest;
import com.studyabroad.entity.User;
import com.studyabroad.repository.UserRepository;
import com.studyabroad.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 用户服务类
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       PermissionService permissionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.permissionService = permissionService;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        if (!permissionService.isSuperAdmin()) {
            throw new RuntimeException("只有超级管理员可以创建账号");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getRole().name());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", savedUser);
        
        return result;
    }

    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!user.getEnabled()) {
            throw new RuntimeException("账户已被禁用");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        
        return result;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User currentUser = permissionService.getCurrentUser();
        boolean isSuperAdmin = permissionService.isSuperAdmin();
        boolean isAdmin = permissionService.isAdmin();

        // 权限检查：只有超级管理员和管理员可以修改其他用户的信息
        // 普通用户只能修改自己的信息
        if (!isSuperAdmin && !isAdmin) {
            if (currentUser == null || !currentUser.getId().equals(id)) {
                throw new RuntimeException("没有权限修改其他用户的信息");
            }
        }

        User user = getUserById(id);
        
        // 只有超级管理员可以修改用户名
        if (userDetails.getUsername() != null && !userDetails.getUsername().trim().isEmpty() 
            && !userDetails.getUsername().equals(user.getUsername())) {
            if (!isSuperAdmin) {
                throw new RuntimeException("只有超级管理员可以修改用户名");
            }
            // 检查新用户名是否已被其他用户使用
            if (userRepository.existsByUsername(userDetails.getUsername().trim())) {
                throw new RuntimeException("用户名已被使用");
            }
            user.setUsername(userDetails.getUsername().trim());
        }
        
        // 只有超级管理员可以修改其他用户的密码和角色
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            if (!isSuperAdmin) {
                throw new RuntimeException("只有超级管理员可以修改用户密码");
            }
            // 如果提供了新密码，加密后更新
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        // 只有超级管理员可以修改用户角色
        if (userDetails.getRole() != null && !userDetails.getRole().equals(user.getRole())) {
            if (!isSuperAdmin) {
                throw new RuntimeException("只有超级管理员可以修改用户角色");
            }
            user.setRole(userDetails.getRole());
        }
        
        // 管理员和超级管理员都可以修改邮箱、姓名、电话等信息
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new RuntimeException("邮箱已被使用");
            }
            user.setEmail(userDetails.getEmail());
        }
        
        if (userDetails.getRealName() != null) {
            user.setRealName(userDetails.getRealName());
        }
        if (userDetails.getPhone() != null) {
            user.setPhone(userDetails.getPhone());
        }
        
        // 只有超级管理员可以修改账户启用状态，但不能停用其他超级管理员
        if (userDetails.getEnabled() != null) {
            if (!isSuperAdmin) {
                throw new RuntimeException("只有超级管理员可以修改账户启用状态");
            }
            // 不能停用其他超级管理员
            if (user.getRole() == User.UserRole.SUPER_ADMIN && !userDetails.getEnabled()) {
                throw new RuntimeException("不能停用其他超级管理员的账户");
            }
            // 不能停用自己
            if (currentUser != null && currentUser.getId().equals(id) && !userDetails.getEnabled()) {
                throw new RuntimeException("不能停用当前登录的账户");
            }
            user.setEnabled(userDetails.getEnabled());
        }
        
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        // 只有超级管理员可以删除用户
        if (!permissionService.isSuperAdmin()) {
            throw new RuntimeException("只有超级管理员可以删除用户");
        }

        User currentUser = permissionService.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id)) {
            throw new RuntimeException("无法删除当前登录的账户");
        }

        User userToDelete = getUserById(id);
        
        // 不允许删除其他超级管理员
        if (userToDelete.getRole() == User.UserRole.SUPER_ADMIN) {
            throw new RuntimeException("不允许删除其他超级管理员账户");
        }
        
        // 允许删除普通管理员、顾问和文案
        
        // 对于文案，applications表的外键约束会自动处理（ON DELETE SET NULL）
        // 删除文案时，其负责的申请记录的writer_id会自动设置为NULL
        
        // 对于顾问，students表和consultation_clients表的外键约束会自动处理（ON DELETE SET NULL）
        // 对于文案，students表和consultation_clients表的外键约束也会自动处理（ON DELETE SET NULL）
        // 删除用户
        userRepository.deleteById(id);
    }

    /**
     * 重置密码：通过用户名和邮箱验证身份后重置密码
     */
    @Transactional
    public void resetPassword(String username, String email, String newPassword) {
        // 根据用户名查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户名不存在"));

        // 验证邮箱是否匹配
        if (!user.getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("邮箱不匹配，无法重置密码");
        }

        // 验证账户是否启用
        if (!user.getEnabled()) {
            throw new RuntimeException("账户已被禁用，无法重置密码");
        }

        // 验证新密码不为空
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }

        // 验证密码长度（至少6位）
        if (newPassword.length() < 6) {
            throw new RuntimeException("新密码长度至少为6位");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

