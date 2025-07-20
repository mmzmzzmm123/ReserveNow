package com.room.hd.controller.admin;

import com.room.hd.common.Result;
import com.room.hd.service.StaffService;
import com.room.hd.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Staff Management Controller
 */
@Slf4j
@RestController
@RequestMapping("/admin/staff")
@CrossOrigin
public class StaffController {
    
    @Autowired
    private StaffService staffService;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    /**
     * Get manager's staff list
     */
    @GetMapping
    public Result<?> getStaffList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(token.replace("Bearer ", ""));
            Integer managerId = claims.get("userId", Integer.class);

            Map<String, Object> result = staffService.getManagerStaffList(managerId, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to get staff list: ", e);
            return Result.error(500,"Failed to get staff list: " + e.getMessage());
        }
    }
    
    /**
     * Review staff application
     */
    @PutMapping("/{staffId}/status")
    public Result<?> updateStaffStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer staffId,
            @RequestParam Integer status) {
        try {
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(token.replace("Bearer ", ""));
            Integer managerId = claims.get("userId", Integer.class);
            
            boolean success = staffService.updateStaffStatus(staffId, managerId, status);
            return success ? Result.success("") : Result.error(500,"Failed to update status");
        } catch (Exception e) {
            log.error("Failed to update staff status: ", e);
            return Result.error(500,"Failed to update staff status: " + e.getMessage());
        }
    }
    
    /**
     * Delete staff
     */
    @DeleteMapping("/{staffId}")
    public Result<?> deleteStaff(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer staffId) {
        try {
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(token.replace("Bearer ", ""));
            Integer managerId = claims.get("userId", Integer.class);
            
            boolean success = staffService.deleteStaff(staffId, managerId);
            return success ? Result.success("") : Result.error(500,"Delete failed");
        } catch (Exception e) {
            log.error("Failed to delete staff: ", e);
            return Result.error(500,"Failed to delete staff: " + e.getMessage());
        }
    }
} 