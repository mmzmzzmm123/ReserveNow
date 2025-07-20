package com.room.hd.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.room.hd.common.Result;
import com.room.hd.dto.UpdateUserDTO;
import com.room.hd.entity.User;
import com.room.hd.mapper.UserMapper;
import com.room.hd.service.UserService;
import com.room.hd.util.JwtTokenUtil;
import com.room.hd.vo.UserVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.wf.captcha.SpecCaptcha;

/**
 * User Service Implementation
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // User role constants
    private static final int ROLE_ADMIN = 0;
    private static final int ROLE_MANAGER = 1;
    private static final int ROLE_USER = 2;
    private static final int ROLE_WAITER = 3;

    // User status constants
    private static final int STATUS_BANNED = 0;
    private static final int STATUS_ENABLED = 1;

    // Map to store captcha, key is captcha ID, value is captcha text
    private Map<String, String> captchaMap = new HashMap<>();

    @Override
    public Result<UserVO> login(String email, String password) {
        // Parameter validation
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(password)) {
            return Result.validateFailed("Email or password cannot be empty");
        }

        // Query user
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        User user = userMapper.selectOne(queryWrapper);

        // User does not exist
        if (user == null) {
            return Result.validateFailed("User does not exist");
        }

        // User is banned
        if (user.getStatus().equals(STATUS_BANNED)) {
            return Result.validateFailed("User is banned");
        }

        // Verify password
        if (!BCrypt.checkpw(password, user.getPassword())) {
            return Result.validateFailed("Incorrect password");
        }

        // Generate token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        String token = jwtTokenUtil.generateToken(user.getEmail(), claims);

        // Build return result
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setUserId(user.getId());
        userVO.setRole(user.getRole());
        userVO.setToken(token);

        return Result.success(userVO, "Login successful");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<UserVO> register(UserVO userVO) {
        // Parameter validation
        if (StringUtils.isEmpty(userVO.getEmail()) || StringUtils.isEmpty(userVO.getPassword())) {
            return Result.validateFailed("Email or password cannot be empty");
        }

        // Password confirmation
        if (!userVO.getPassword().equals(userVO.getConfirmPassword())) {
            return Result.validateFailed("Passwords do not match");
        }

        // Email format validation
        if (!userVO.getEmail().matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            return Result.validateFailed("Invalid email format");
        }

        // Check if email is already registered
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, userVO.getEmail());
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            return Result.validateFailed("Email is already registered");
        }

        // Create user entity
        User user = new User();
        user.setName(userVO.getName());
        user.setEmail(userVO.getEmail());
        // Encrypt password
        user.setPassword(BCrypt.hashpw(userVO.getPassword(), BCrypt.gensalt()));

        // Set user role and status as integer values
        user.setRole(ROLE_USER);
        user.setStatus(STATUS_ENABLED);

        // Use OffsetDateTime
        user.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Save user
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            log.error("User registration failed", e);
            return Result.serverError();
        }

        // Build return result
        UserVO result = new UserVO();
        BeanUtils.copyProperties(user, result);
        result.setUserId(user.getId());
        result.setRole(user.getRole());

        return Result.success(result, "Registration successful");
    }

    @Override
    public Result<UserVO> getUserInfo(Integer userId) {
        if (userId == null) {
            return Result.validateFailed("User ID cannot be empty");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.validateFailed("User does not exist");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setUserId(user.getId());
        userVO.setRole(user.getRole());
        userVO.setStatus((user.getStatus()));

        return Result.success(userVO, "Retrieved successfully");
    }

    @Override
    public Result<Map> getCaptcha() {
        try {
            // Create SpecCaptcha, width 130, height 48, captcha length 5
            SpecCaptcha captcha = new SpecCaptcha(120, 48, 5);
            // Set font
            captcha.setFont(new java.awt.Font("Verdana", java.awt.Font.PLAIN, 32));
            // Set type, numbers and uppercase letters only
            captcha.setCharType(SpecCaptcha.TYPE_NUM_AND_UPPER);

            // Get captcha text
            String captchaText = captcha.text();

            // Convert to Base64 encoding
            String base64Image = captcha.toBase64();

            captchaMap.put(captchaText, captchaText);

            // Return captcha ID and image
            Map<String, String> result = new HashMap<>();
            result.put("captchaId", captchaText);
            result.put("image", base64Image);

            return Result.success(result, "Captcha retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to generate captcha", e);
            return Result.serverError();
        }
    }

    /**
     * Get role name
     *
     * @param role Role code
     * @return Role name
     */
    private String getRoleName(Integer role) {
        if (role == null) {
            return "Unknown";
        }

        switch (role) {
            case ROLE_ADMIN:
                return "Administrator";
            case ROLE_MANAGER:
                return "Restaurant Manager";
            case ROLE_USER:
                return "User";
            case ROLE_WAITER:
                return "Waiter";
            default:
                return "Unknown";
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<UserVO> updateUserInfo(Integer userId, UpdateUserDTO updateUserDTO) {
        // Parameter validation
        if (userId == null) {
            return Result.validateFailed("User ID cannot be empty");
        }

        // Query user
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.validateFailed("User does not exist");
        }

        // Update basic information
        boolean hasUpdates = false;
        if (StringUtils.hasText(updateUserDTO.getName())) {
            user.setName(updateUserDTO.getName());
            hasUpdates = true;
        }
        if (StringUtils.hasText(updateUserDTO.getAvatar())) {
            user.setAvatar(updateUserDTO.getAvatar());
            hasUpdates = true;
        }

        // Handle password update
        if (StringUtils.hasText(updateUserDTO.getOldPassword()) &&
                StringUtils.hasText(updateUserDTO.getNewPassword()) &&
                StringUtils.hasText(updateUserDTO.getConfirmPassword())) {

            // Verify old password
            if (!BCrypt.checkpw(updateUserDTO.getOldPassword(), user.getPassword())) {
                return Result.validateFailed("Incorrect old password");
            }

            // Verify new password confirmation
            if (!updateUserDTO.getNewPassword().equals(updateUserDTO.getConfirmPassword())) {
                return Result.validateFailed("Passwords do not match");
            }

            // Update password
            user.setPassword(BCrypt.hashpw(updateUserDTO.getNewPassword(), BCrypt.gensalt()));
            hasUpdates = true;
        } else if (StringUtils.hasText(updateUserDTO.getOldPassword()) ||
                StringUtils.hasText(updateUserDTO.getNewPassword()) ||
                StringUtils.hasText(updateUserDTO.getConfirmPassword())) {
            // If partial password fields are provided but not complete
            return Result.validateFailed("Updating password requires: old password, new password, and confirm password");
        }

        // Update timestamp if there are changes
        if (hasUpdates) {
            user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            try {
                userMapper.updateById(user);
            } catch (Exception e) {
                log.error("Failed to update user information", e);
                return Result.error(500, "Failed to update user information: " + e.getMessage());
            }
        }

        // Build return result
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setUserId(user.getId());
        userVO.setRole(user.getRole());

        return Result.success(userVO, "Updated successfully");
    }

    @Override
    public void repassword(String newPassword, String oldPassword, Integer userId) {
        User user = userMapper.selectById(userId);
        if (user.getPassword().equals(oldPassword)) {
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        } else {
            throw new RuntimeException("Incorrect old password");
        }
    }
}