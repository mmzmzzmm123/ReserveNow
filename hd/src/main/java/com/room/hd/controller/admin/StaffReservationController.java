package com.room.hd.controller.admin;

import com.room.hd.common.Result;
import com.room.hd.entity.Reservation;
import com.room.hd.service.ReservationService;
import com.room.hd.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Staff Reservation Management Controller
 */
@Slf4j
@RestController
@RequestMapping("/admin/staff/reservations")
public class StaffReservationController {

    @Resource
    private ReservationService reservationService;
    
    @Resource
    private JwtTokenUtil jwtUtil;

    /**
     * Get reservation list for staff's restaurant
     */
    @GetMapping
    public Result<Map<String, Object>> getStaffReservations(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer restaurantId) {
        try {
            // Verify token and get user information
            Map<String, Object> userInfo = jwtUtil.getAllClaimsFromToken(token);
            if (userInfo == null) {
                return Result.error(401, "Unauthorized or invalid token");
            }
            
            // Verify if user is staff
            Integer role = (Integer) userInfo.get("role");
            if (role != 3) { // 3 represents staff role
                return Result.error(403, "Insufficient permissions, only staff can access this interface");
            }
            
            // Get reservation list
            Map<String, Object> result = reservationService.getStaffReservations(
                    (Integer) userInfo.get("userId"),
                    page,
                    pageSize,
                    status,
                    restaurantId
            );
            
            return Result.success(result, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get staff reservation list", e);
            return Result.error(500, "Failed to get staff reservation list: " + e.getMessage());
        }
    }

    /**
     * Update reservation status
     */
    @PutMapping("/{reservationId}/status")
    public Result<Reservation> updateReservationStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer reservationId,
            @RequestParam Integer status) {
        try {
            // Verify token and get user information
            Map<String, Object> userInfo = jwtUtil.getAllClaimsFromToken(token);
            if (userInfo == null) {
                return Result.error(401, "Unauthorized or invalid token");
            }
            
            // Verify if user is staff
            Integer role = (Integer) userInfo.get("role");
            if (role != 3) { // 3 represents staff role
                return Result.error(403, "Insufficient permissions, only staff can access this interface");
            }
            
            // Update reservation status
            Reservation updatedReservation = reservationService.updateStaffReservationStatus(
                    (Integer) userInfo.get("userId"),
                    reservationId,
                    status
            );
            
            return Result.success(updatedReservation, "Updated successfully");
        } catch (Exception e) {
            log.error("Failed to update reservation status", e);
            return Result.error(500, "Failed to update reservation status: " + e.getMessage());
        }
    }
} 