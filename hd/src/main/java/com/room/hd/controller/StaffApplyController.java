package com.room.hd.controller;

import com.room.hd.common.Result;
import com.room.hd.dto.StaffApplyDTO;
import com.room.hd.service.StaffService;
import com.room.hd.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Staff Application Controller
 */
@Slf4j
@RestController
@RequestMapping("/staff-apply")
public class StaffApplyController {

    @Resource
    private StaffService staffService;
    
    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * Get restaurant manager list
     * 
     * @param page Page number, default 1
     * @param pageSize Items per page, default 10
     * @param keyword Search keyword (optional)
     * @return Restaurant manager list
     */
    @GetMapping("/managers")
    public Result<Map<String, Object>> getManagerList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            Map<String, Object> result = staffService.getManagerList(page, pageSize, keyword);
            return Result.success(result, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get restaurant manager list: ", e);
            return Result.error(500, "Failed to get restaurant manager list: " + e.getMessage());
        }
    }
    
    /**
     * Submit staff application
     * 
     * @param token JWT token
     * @param managerId Manager ID
     * @return Application result
     */
    @PostMapping
    public Result<?> applyForStaff(
            @RequestHeader("Authorization") String token,
            @RequestParam Integer managerId) {
        try {
            // Verify token and get user information
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(token.replace("Bearer ", ""));
            Integer userId = claims.get("userId", Integer.class);
            Integer role = claims.get("role", Integer.class);
            
            // Create application DTO
            StaffApplyDTO staffApplyDTO = new StaffApplyDTO();
            staffApplyDTO.setUserId(userId);
            staffApplyDTO.setManagerId(managerId);
            
            // Submit application
            return staffService.applyForStaff(staffApplyDTO);
        } catch (Exception e) {
            log.error("Failed to submit staff application: ", e);
            return Result.error(500, "Failed to submit staff application: " + e.getMessage());
        }
    }
} 