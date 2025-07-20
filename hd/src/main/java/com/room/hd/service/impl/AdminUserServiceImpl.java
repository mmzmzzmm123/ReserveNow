package com.room.hd.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.room.hd.common.Result;
import com.room.hd.entity.User;
import com.room.hd.mapper.UserMapper;
import com.room.hd.service.AdminUserService;
import com.room.hd.util.JwtTokenUtil;
import com.room.hd.vo.UserVO;
import io.jsonwebtoken.Claims;
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
import java.util.List;
import java.util.Map;

/**
 * Administrator User Service Implementation
 */
@Service
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private UserMapper userMapper;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Override
    public Result<Map<String, Object>> getUserList(Integer page, Integer pageSize, String keyword, Integer role, Integer status) {
        try {
            // Parameter validation
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }
            
            // Calculate offset
            int offset = (page - 1) * pageSize;
            
            // Use custom SQL query
            List<User> userList = userMapper.getUserList(offset, pageSize, keyword, role, status);
            long total = userMapper.countUsers(keyword, role, status);
            
            // Convert to VO objects
            List<UserVO> userVOList = new ArrayList<>();
            for (User user : userList) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                userVO.setUserId(user.getId());
                userVO.setRole(user.getRole());
                userVO.setRoleValue(user.getRole());
                userVO.setStatus(user.getStatus());
                userVO.setStatusValue(user.getStatus());
                userVOList.add(userVO);
            }
            
            // Build return result
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", userVOList);
            
            return Result.success(result, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get user list", e);
            return Result.error(500, "Failed to get user list: " + e.getMessage());
        }
    }
    
    @Override
    public Result<UserVO> getUserInfo(Integer userId) {
        try {
            // Parameter validation
            if (userId == null) {
                return Result.validateFailed("User ID cannot be empty");
            }
            
            // Query user
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.validateFailed("User not found");
            }
            
            // Convert to VO object
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            userVO.setUserId(user.getId());
            userVO.setRole(user.getRole());
            userVO.setRoleValue(user.getRole());
            userVO.setStatus(user.getStatus());
            userVO.setStatusValue(user.getStatus());
            
            return Result.success(userVO, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get user information", e);
            return Result.error(500, "Failed to get user information: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<UserVO> addUser(UserVO userVO) {
        try {
            // Parameter validation
            if (StringUtils.isEmpty(userVO.getEmail()) || StringUtils.isEmpty(userVO.getPassword())) {
                return Result.validateFailed("Email or password cannot be empty");
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
                return Result.validateFailed("Email already registered");
            }
            
            // Create user entity
            User user = new User();
            user.setName(userVO.getName());
            user.setEmail(userVO.getEmail());
            // Encrypt password
            user.setPassword(BCrypt.hashpw(userVO.getPassword(), BCrypt.gensalt()));
            
            // Set user role and status
            user.setRole(userVO.getRoleValue() != null ? userVO.getRoleValue() : 2);
            user.setStatus(userVO.getStatusValue() != null ? userVO.getStatusValue() : 1);
            
            // Set avatar
            if (userVO.getAvatar() != null) {
                user.setAvatar(userVO.getAvatar());
            }
            
            // Set creation and update time
            user.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            
            // Save user
            userMapper.insert(user);
            
            // Build return result
            UserVO result = new UserVO();
            BeanUtils.copyProperties(user, result);
            result.setUserId(user.getId());
            result.setRole(user.getRole());
            result.setRoleValue(user.getRole());
            result.setStatus(user.getStatus());
            result.setStatusValue(user.getStatus());
            
            return Result.success(result, "Added successfully");
        } catch (Exception e) {
            log.error("Failed to add user", e);
            return Result.error(500, "Failed to add user: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<UserVO> updateUser(Integer userId, UserVO userVO) {
        try {
            // Parameter validation
            if (userId == null) {
                return Result.validateFailed("User ID cannot be empty");
            }
            
            // Query user
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.validateFailed("User not found");
            }
            
            // Email uniqueness check
            if (userVO.getEmail() != null && !userVO.getEmail().equals(user.getEmail())) {
                // Email format validation
                if (!userVO.getEmail().matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
                    return Result.validateFailed("Invalid email format");
                }
                
                // Check if email is used by other users
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getEmail, userVO.getEmail())
                        .ne(User::getId, userId);
                long count = userMapper.selectCount(queryWrapper);
                if (count > 0) {
                    return Result.validateFailed("Email already used by another user");
                }
                
                user.setEmail(userVO.getEmail());
            }
            
            // Update username
            if (userVO.getName() != null) {
                user.setName(userVO.getName());
            }
            
            // Update avatar
            if (userVO.getAvatar() != null) {
                user.setAvatar(userVO.getAvatar());
            }
            
            // Update role
            if (userVO.getRoleValue() != null) {
                user.setRole(userVO.getRoleValue());
            }
            
            // Update status
            if (userVO.getStatusValue() != null) {
                user.setStatus(userVO.getStatusValue());
            }
            
            // Update password (if new password provided)
            if (userVO.getPassword() != null && !userVO.getPassword().isEmpty()) {
                user.setPassword(BCrypt.hashpw(userVO.getPassword(), BCrypt.gensalt()));
            }
            
            // Update time
            user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            
            // Save update
            userMapper.updateById(user);
            
            // Build return result
            UserVO result = new UserVO();
            BeanUtils.copyProperties(user, result);
            result.setUserId(user.getId());
            result.setRole(user.getRole());
            result.setRoleValue(user.getRole());
            result.setStatus(user.getStatus());
            result.setStatusValue(user.getStatus());
            
            return Result.success(result, "Updated successfully");
        } catch (Exception e) {
            log.error("Failed to update user", e);
            return Result.error(500, "Failed to update user: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteUser(Integer userId) {
        try {
            // Parameter validation
            if (userId == null) {
                return Result.validateFailed("User ID cannot be empty");
            }
            
            // Query user
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.validateFailed("User not found");
            }
            
            // Prevent deleting system administrator
            if (user.getRole() == 0) {
                return Result.validateFailed("Cannot delete system administrator");
            }
            
            // Delete user
            userMapper.deleteById(userId);
            
            return Result.success(null, "Deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete user", e);
            return Result.error(500, "Failed to delete user: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateUserStatus(Integer userId, Integer status) {
        try {
            // Parameter validation
            if (userId == null) {
                return Result.validateFailed("User ID cannot be empty");
            }

            // Query user
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.validateFailed("User not found");
            }
            
            // Prevent disabling system administrator
            if (user.getRole() == 0 && status == 0) {
                return Result.validateFailed("Cannot disable system administrator");
            }
            
            // Update status
            user.setStatus(status);
            user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            userMapper.updateById(user);
            
            // Build return data
            Map<String, Object> result = new HashMap<>();
            result.put("userId", user.getId());
            result.put("status", status);
            result.put("statusText", status);
            
            return Result.success(result, "Status updated successfully");
        } catch (Exception e) {
            log.error("Failed to update user status", e);
            return Result.error(500, "Failed to update user status: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateUserRole(Integer userId, Integer role) {
        try {
            // Parameter validation
            if (userId == null) {
                return Result.validateFailed("User ID cannot be empty");
            }
            
            if (role == null || role < 0 || role > 3) {
                return Result.validateFailed("Invalid role parameter");
            }
            
            // Query user
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.validateFailed("User not found");
            }
            
            // Update role
            user.setRole(role);
            user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            userMapper.updateById(user);
            
            // Build return data
            Map<String, Object> result = new HashMap<>();
            result.put("userId", user.getId());
            result.put("role", role);
            result.put("roleText", role);
            
            return Result.success(result, "Role updated successfully");
        } catch (Exception e) {
            log.error("Failed to update user role", e);
            return Result.error(500, "Failed to update user role: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> resetUserPassword(Integer userId) {
        try {
            // Parameter validation
            if (userId == null) {
                return Result.validateFailed("User ID cannot be empty");
            }
            
            // Query user
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.validateFailed("User not found");
            }
            
            // Reset password, default password is "123456"
            String defaultPassword = "123456";
            user.setPassword(BCrypt.hashpw(defaultPassword, BCrypt.gensalt()));
            user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            userMapper.updateById(user);
            
            return Result.success(null, "Password reset successfully, new password is: " + defaultPassword);
        } catch (Exception e) {
            log.error("Failed to reset user password", e);
            return Result.error(500, "Failed to reset user password: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Integer> checkAdminRole(String token) {
        // Token validation
        if (token == null || token.isEmpty()) {
            return Result.unauthorized();
        }
        
        try {
            // Parse user role from token
            Claims allClaimsFromToken = jwtTokenUtil.getAllClaimsFromToken(token);
            Integer role = allClaimsFromToken.get("role", Integer.class);
            if (role == null || role != 0) {
                return Result.forbidden("No permission to access administrator interface");
            }
            
            // Parse user ID
            Integer userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.unauthorized();
            }
            
            return Result.success(userId);
        } catch (Exception e) {
            log.error("Failed to parse JWT token", e);
            return Result.unauthorized();
        }
    }
} 