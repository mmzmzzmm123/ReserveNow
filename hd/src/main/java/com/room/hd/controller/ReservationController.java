package com.room.hd.controller;

import com.room.hd.common.Result;
import com.room.hd.dto.CancelReservationDTO;
import com.room.hd.dto.ReservationDTO;
import com.room.hd.service.ReservationService;
import com.room.hd.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Reservation Controller
 */
@RestController
@RequestMapping("/reservations")
@CrossOrigin
public class ReservationController {

    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    /**
     * Create reservation
     * @param reservationDTO Reservation information
     * @param request HTTP request containing user JWT token
     * @return Reservation result
     */
    @PostMapping
    public Result<?> createReservation(@RequestBody ReservationDTO reservationDTO, HttpServletRequest request) {
        // Get token from request header
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return Result.unauthorized();
        }
        
        try {
            // Parse user ID from token
            Integer userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.unauthorized();
            }
            
            // Create reservation
            return reservationService.createReservation(reservationDTO, userId);
        } catch (Exception e) {
            return Result.error(500, "Failed to create reservation: " + e.getMessage());
        }
    }
    
    /**
     * Get user's reservation list
     * @param page Current page number
     * @param pageSize Items per page
     * @param status Reservation status, comma-separated numbers for multiple statuses
     * @param request HTTP request containing user JWT token
     * @return Reservation list
     */
    @GetMapping
    public Result<?> getUserReservations(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "status", required = false) String status,
            HttpServletRequest request) {
        
        // Get token from request header
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return Result.unauthorized();
        }
        
        try {
            // Parse user ID from token
            Integer userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.unauthorized();
            }
            
            // Get user's reservation list
            return reservationService.getUserReservations(page, pageSize, status, userId);
        } catch (Exception e) {
            return Result.error(500, "Failed to get reservation list: " + e.getMessage());
        }
    }
    
    /**
     * Cancel reservation
     * @param reservationId Reservation ID
     * @param cancelReservationDTO Cancellation reason
     * @param request HTTP request containing user JWT token
     * @return Cancellation result
     */
    @PutMapping("/{reservationId}/cancel")
    public Result<?> cancelReservation(
            @PathVariable Integer reservationId,
            @RequestBody CancelReservationDTO cancelReservationDTO,
            HttpServletRequest request) {
        
        // Get token from request header
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return Result.unauthorized();
        }
        
        try {
            // Parse user ID from token
            Integer userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.unauthorized();
            }
            
            // Cancel reservation
            return reservationService.cancelReservation(reservationId, cancelReservationDTO.getCancelReason(), userId);
        } catch (Exception e) {
            return Result.error(500, "Failed to cancel reservation: " + e.getMessage());
        }
    }
    
    /**
     * Get all future reservations for a table
     * @param tableId Table ID
     * @return Reservation list
     */
    @GetMapping("/table/{tableId}/future")
    public Result<?> getTableFutureReservations(@PathVariable Integer tableId) {
        if (tableId == null) {
            return Result.validateFailed("Table ID cannot be empty");
        }
        
        return reservationService.getTableFutureReservations(tableId);
    }
} 