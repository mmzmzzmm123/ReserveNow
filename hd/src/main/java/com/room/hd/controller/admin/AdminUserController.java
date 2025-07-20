package com.room.hd.controller.admin;

import com.room.hd.common.Result;
import com.room.hd.service.AdminUserService;
import com.room.hd.util.JwtTokenUtil;
import com.room.hd.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Administrator User Management Controller
 */
@RestController
@RequestMapping("/admin/users")
@CrossOrigin
public class AdminUserController  {

    @Autowired
    private AdminUserService userService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    /**
     * Get user list
     * @param page Page number
     * @param pageSize Items per page
     * @param keyword Search keyword (username or email)
     * @param role Role filter (optional)
     * @param status Status filter (optional)
     * @param request HTTP request containing user JWT token
     * @return User list
     */
    @GetMapping
    public Result<Map<String, Object>> getUserList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) Integer role,
            @RequestParam(value = "status", required = false) Integer status,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Integer> roleCheck = checkAdminRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access administrator interface");
        }
        
        // Get user list
        return userService.getUserList(page, pageSize, keyword, role, status);
    }
    
    /**
     * Get user details
     * @param userId User ID
     * @param request HTTP request containing user JWT token
     * @return User details
     */
    @GetMapping("/{userId}")
    public Result<UserVO> getUserDetail(
            @PathVariable Integer userId,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Integer> roleCheck = checkAdminRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access administrator interface");
        }
        
        // Get user details
        return userService.getUserInfo(userId);
    }
    
    /**
     * Add user
     * @param userVO User information
     * @param request HTTP request containing user JWT token
     * @return Add result
     */
    @PostMapping
    public Result<UserVO> addUser(
            @RequestBody UserVO userVO,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Integer> roleCheck = checkAdminRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access administrator interface");
        }
        
        // Add user
        return userService.addUser(userVO);
    }
    
    /**
     * Update user information
     * @param userId User ID
     * @param userVO User information
     * @param request HTTP request containing user JWT token
     * @return Update result
     */
    @PutMapping("/{userId}")
    public Result<UserVO> updateUser(
            @PathVariable Integer userId,
            @RequestBody UserVO userVO,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Integer> roleCheck = checkAdminRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access administrator interface");
        }
        
        // Set user ID
        userVO.setUserId(userId);
        
        // Update user information
        return userService.updateUser(userId, userVO);
    }
    
    /**
     * Delete user
     * @param userId User ID
     * @param request HTTP request containing user JWT token
     * @return Delete result
     */
    @DeleteMapping("/{userId}")
    public Result<?> deleteUser(
            @PathVariable Integer userId,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Integer> roleCheck = checkAdminRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access administrator interface");
        }
        
        // Delete user
        return userService.deleteUser(userId);
    }
    
    /**
     * Update user status (Enable/Disable)
     * @param userId User ID
     * @param status Status (0: Disabled, 1: Enabled)
     * @param request HTTP request containing user JWT token
     * @return Update result
     */
    @PutMapping("/{userId}/status")
    public Result<?> updateUserStatus(
            @PathVariable Integer userId,
            @RequestParam Integer status,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Integer> roleCheck = checkAdminRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access administrator interface");
        }
        
        // Update user status
        return userService.updateUserStatus(userId, status);
    }
    
    /**
     * Update user role
     * @param userId User ID
     * @param role Role (0: Administrator, 1: Restaurant Manager, 2: User, 3: Staff)
     * @param request HTTP request containing user JWT token
     * @return Update result
     */
    @PutMapping("/{userId}/role")
    public Result<?> updateUserRole(
            @PathVariable Integer userId,
            @RequestParam Integer role,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Integer> roleCheck = checkAdminRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access administrator interface");
        }
        
        // Update user role
        return userService.updateUserRole(userId, role);
    }
    
    /**
     * Reset user password
     * @param userId User ID
     * @param request HTTP request containing user JWT token
     * @return Reset result
     */
    @PutMapping("/{userId}/reset-password")
    public Result<?> resetUserPassword(
            @PathVariable Integer userId,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Integer> roleCheck = checkAdminRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access administrator interface");
        }
        
        // Reset user password
        return userService.resetUserPassword(userId);
    }

    /**
     * Check if user has administrator role
     * @param request HTTP request containing user JWT token
     * @return Check result
     */
    private Result<Integer> checkAdminRole(HttpServletRequest request) {
        // Get token from request header
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return Result.unauthorized();
        }

        try {
            // Parse user ID
            Integer userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.unauthorized();
            }
            
            // Get role directly from token, keep numeric value without mapping
            Integer role = jwtTokenUtil.getClaimFromToken(token, claims -> claims.get("role", Integer.class));
            System.out.println("role=" + role);
            if (role == null || role != 0) {
                return Result.forbidden();
            }
            
            return Result.success(userId);
        } catch (Exception e) {
            return Result.unauthorized();
        }
    }
} 