package com.room.hd.service;

import com.room.hd.common.Result;
import com.room.hd.dto.UpdateUserDTO;
import com.room.hd.vo.UserVO;

import java.util.Map;

/**
 * User Service Interface
 */
public interface UserService {

    /**
     * User login
     * @param email Email
     * @param password Password
     * @return Login result
     */
    Result<UserVO> login(String email, String password);

    /**
     * User registration
     * @param userVO Registration parameters
     * @return Registration result
     */
    Result<UserVO> register(UserVO userVO);

    /**
     * Get user information
     * @param userId User ID
     * @return User information
     */
    Result<UserVO> getUserInfo(Integer userId);
    
    /**
     * Get verification code
     *
     * @return Base64 encoded verification code image
     */
    Result<Map> getCaptcha();
    
    /**
     * Update user information
     * @param userId User ID
     * @param updateUserDTO Update information
     * @return Update result
     */
    Result<UserVO> updateUserInfo(Integer userId, UpdateUserDTO updateUserDTO);

    /**
     * Change password
     * @param newPassword New password
     * @param oldPassword Old password
     * @param userId User ID
     */
    void repassword(String newPassword, String oldPassword, Integer userId);
} 