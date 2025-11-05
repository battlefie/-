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

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
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
        User user = getUserById(id);
        
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
        if (userDetails.getEnabled() != null) {
            user.setEnabled(userDetails.getEnabled());
        }
        
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
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

