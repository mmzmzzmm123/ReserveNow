package com.room.hd.service;

import com.room.hd.common.Result;
import com.room.hd.vo.UserVO;

import java.util.Map;

/**
 * Administrator User Service Interface
 */
public interface AdminUserService {
    
    /**
     * Get user list
     * @param page Page number
     * @param pageSize Items per page
     * @param keyword Search keyword (username or email)
     * @param role Role filter (optional)
     * @param status Status filter (optional)
     * @return User list result
     */
    Result<Map<String, Object>> getUserList(Integer page, Integer pageSize, String keyword, Integer role, Integer status);
    
    /**
     * Get user details
     * @param userId User ID
     * @return User details
     */
    Result<UserVO> getUserInfo(Integer userId);
    
    /**
     * Add user
     * @param userVO User information
     * @return Addition result
     */
    Result<UserVO> addUser(UserVO userVO);
    
    /**
     * Update user information
     * @param userId User ID
     * @param userVO User information
     * @return Update result
     */
    Result<UserVO> updateUser(Integer userId, UserVO userVO);
    
    /**
     * Delete user
     * @param userId User ID
     * @return Deletion result
     */
    Result<?> deleteUser(Integer userId);
    
    /**
     * Update user status (enable/disable)
     * @param userId User ID
     * @param status Status (0:Disabled, 1:Enabled)
     * @return Update result
     */
    Result<?> updateUserStatus(Integer userId, Integer status);
    
    /**
     * Update user role
     * @param userId User ID
     * @param role Role (0:Administrator, 1:Restaurant Manager, 2:User, 3:Staff)
     * @return Update result
     */
    Result<?> updateUserRole(Integer userId, Integer role);
    
    /**
     * Reset user password
     * @param userId User ID
     * @return Reset result
     */
    Result<?> resetUserPassword(Integer userId);
    
    /**
     * Check administrator permission
     * @param token JWT token
     * @return Check result, returns user ID if successful
     */
    Result<Integer> checkAdminRole(String token);
} 