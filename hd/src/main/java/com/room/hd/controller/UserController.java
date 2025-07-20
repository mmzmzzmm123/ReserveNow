package com.room.hd.controller;

import com.room.hd.common.Result;
import com.room.hd.dto.UpdateUserDTO;
import com.room.hd.service.UserService;
import com.room.hd.util.JwtTokenUtil;
import com.room.hd.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * User Controller
 */
@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * User login
     *
     * @param userVO Login parameters
     * @return Login result
     */
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody UserVO userVO) {
        return userService.login(userVO.getEmail(), userVO.getPassword());
    }

    /**
     * User registration
     *
     * @param userVO Registration parameters
     * @return Registration result
     */
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody UserVO userVO) {
        return userService.register(userVO);
    }

    /**
     * Get verification code
     *
     * @return Base64 encoded verification code image
     */
    @GetMapping("/captcha")
    public Result<Map> getCaptcha() {
        return userService.getCaptcha();
    }

    /**
     * Get user information
     *
     * @param request HTTP request
     * @return User information
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        // Get user ID from request
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty())
            return Result.unauthorized();
        try {
            Integer userId = jwtTokenUtil.getUserIdFromToken(token);
            return userService.getUserInfo(userId);
        } catch (Exception e) {
            return Result.error(500, "Failed to get user information: " + e.getMessage());
        }
    }

    /**
     * Update user information
     *
     * @param updateUserDTO Update information
     * @param request HTTP request
     * @return Update result
     */
    @PutMapping("/update")
    public Result<UserVO> updateUserInfo(@RequestBody UpdateUserDTO updateUserDTO, HttpServletRequest request) {
        // Get user ID from request
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return Result.unauthorized();
        }

        try {
            Integer userId = jwtTokenUtil.getUserIdFromToken(token);
            return userService.updateUserInfo(userId, updateUserDTO);
        } catch (Exception e) {
            return Result.error(500, "Failed to update user information: " + e.getMessage());
        }
    }

    @PutMapping("/change-password")
    public Result<?> changePassword(@RequestBody Map<String, String> map, HttpServletRequest request) {
        // Get user ID from request
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return Result.unauthorized();
        }

        try {
            Integer userId = jwtTokenUtil.getUserIdFromToken(token);
            userService.repassword(map.get("newPassword"), map.get("oldPassword"),userId);
        } catch (Exception e) {
            return Result.error(500, "Failed to update user information: " + e.getMessage());
        }
        return Result.success("Password changed successfully");
    }
}