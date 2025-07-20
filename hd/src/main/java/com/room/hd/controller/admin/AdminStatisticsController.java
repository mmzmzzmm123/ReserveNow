package com.room.hd.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.room.hd.common.Result;
import com.room.hd.dto.StatisticsDTO;
import com.room.hd.entity.Staff;
import com.room.hd.mapper.StaffMapper;
import com.room.hd.mapper.UserMapper;
import com.room.hd.service.AdminService;
import com.room.hd.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Administrator Statistics Controller
 */
@RestController
@RequestMapping("/admin/statistics")
@CrossOrigin
public class AdminStatisticsController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private  StaffMapper staffMapper;

    /**
     * Get system statistics
     *
     * @param request HTTP request containing user JWT token
     * @return Statistics data
     */
    @GetMapping("/overview")
    public Result<StatisticsDTO> getStatistics(HttpServletRequest request) {
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkAdminRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access administrator interface");
        }

        // Get statistics data
        Map<String, Object> userInfo = roleCheck.getData();
        // If user is staff, get their manager ID
        System.out.println("userInfo:" + userInfo.get("role"));
        if (roleCheck.getData().get("role").equals(3)) {
            LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Staff::getUserId, userInfo.get("userId"));
            Staff staff = staffMapper.selectOne(queryWrapper);
            if (staff == null) return Result.error(401, "You haven't applied for a manager yet");
            if (staff.getManagerId() == null) return Result.error(401, "You haven't applied for a manager yet");
            if (staff.getStatus() == 0) return Result.error(401, "Your manager application hasn't been approved yet");
            System.out.println("Manager ID：" + staff.getManagerId());
            userInfo.put("userId", staff.getManagerId());
        }
        return adminService.getStatistics(userInfo);
    }

    /**
     * Get reservation statistics for the last 7 days
     *
     * @param request HTTP request containing user JWT token
     * @return Reservation statistics for the last 7 days
     */
    @GetMapping("/reservations")
    public Result<Map<String, Object>> getReservationStatistics(HttpServletRequest request) {
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkAdminRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access administrator interface");
        }

        // Get reservation statistics
        Map<String, Object> userInfo = roleCheck.getData();
        // If user is staff, get their manager ID
        if (roleCheck.getData().get("role").equals(3)) {
            LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Staff::getUserId, userInfo.get("userId"));
            Staff staff = staffMapper.selectOne(queryWrapper);
            if (staff == null) return Result.error(401, "You haven't applied for a manager yet");
            if (staff.getManagerId() == null) return Result.error(401, "You haven't applied for a manager yet");
            if (staff.getStatus() == 0) return Result.error(401, "Your manager application hasn't been approved yet");
            userInfo.put("userId", staff.getManagerId());
        }
        return adminService.getReservationStatistics(userInfo);
    }

    /**
     * Get restaurant statistics for the last 7 days
     *
     * @param request HTTP request containing user JWT token
     * @return Restaurant statistics for the last 7 days
     */
    @GetMapping("/restaurants")
    public Result<Map<String, Object>> getRestaurantStatistics(HttpServletRequest request) {
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkAdminRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access administrator interface");
        }

        // Get restaurant statistics
        Map<String, Object> userInfo = roleCheck.getData();
        // If user is staff, get their manager ID
        if (roleCheck.getData().get("role").equals(3)) {
            LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Staff::getUserId, userInfo.get("userId"));
            Staff staff = staffMapper.selectOne(queryWrapper);
            if (staff == null) return Result.error(401, "You haven't applied for a manager yet");
            if (staff.getManagerId() == null) return Result.error(401, "You haven't applied for a manager yet");
            if (staff.getStatus() == 0) return Result.error(401, "Your manager application hasn't been approved yet");
            userInfo.put("userId", staff.getManagerId());
        }
        return adminService.getRestaurantStatistics(userInfo);
    }

    /**
     * Check if user has administrator or restaurant manager role and return user information
     *
     * @param request HTTP request containing user JWT token
     * @return Check result containing user role and ID information
     */
    private Result<Map<String, Object>> checkAdminRole(HttpServletRequest request) {
        // Get token from request header
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return Result.unauthorized();
        }

        try {
            Claims allClaimsFromToken = jwtTokenUtil.getAllClaimsFromToken(token);
            Integer role = (Integer) allClaimsFromToken.get("role");
            Integer userId = (Integer) allClaimsFromToken.get("userId");

            // Build user information Map
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("role", role);
            userInfo.put("userId", userId);
            
            return Result.success(userInfo);
        } catch (Exception e) {
            return Result.unauthorized();
        }
    }
} 