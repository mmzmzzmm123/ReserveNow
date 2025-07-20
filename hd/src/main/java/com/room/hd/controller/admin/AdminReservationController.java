package com.room.hd.controller.admin;


import com.room.hd.common.Result;
import com.room.hd.service.FavoriteService;
import com.room.hd.service.ReservationService;
import com.room.hd.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Reservation Management Controller
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminReservationController {
    private final Logger logger = LoggerFactory.getLogger(AdminReservationController.class);

    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * Get all reservations (Administrator only)
     * @param page Page number, default 1
     * @param pageSize Items per page, default 10
     * @param status Reservation status, multiple statuses separated by commas (0: Cancelled, 1: Pending, 2: Confirmed, 3: Completed, 4: Rejected)
     * @param restaurantId Restaurant ID, if provided, only query reservations for this restaurant
     * @param request Request object for getting token
     * @return Reservation list
     */
    @GetMapping("/reservations")
    public Result<?> getAllReservations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer restaurantId,
            HttpServletRequest request
    ) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.unauthorized();
            }

            // Get user information from token
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(token);
            Integer role = claims.get("role", Integer.class);
            
            // Verify role is administrator
            if (role == null || role != 0) {
                return Result.forbidden("No permission to access, this interface is for administrators only");
            }

            Map<String, Object> result = reservationService.getAllReservations(page, pageSize, status, restaurantId);
            return Result.success(result, "Retrieved successfully");
        } catch (Exception e) {
            logger.error("Failed to get reservation list: ", e);
            return Result.error(500, "Failed to get reservation list: " + e.getMessage());
        }
    }

    /**
     * Get reservations for restaurants owned by restaurant manager
     * @param page Page number, default 1
     * @param pageSize Items per page, default 10
     * @param status Reservation status, multiple statuses separated by commas (0: Cancelled, 1: Pending, 2: Confirmed, 3: Completed, 4: Rejected)
     * @param restaurantId Restaurant ID, if provided, only query reservations for this restaurant, which must belong to current restaurant manager
     * @param request Request object for getting token
     * @return Reservation list
     */
    @GetMapping("/reservations/manager")
    public Result<?> getManagerReservations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer restaurantId,
            HttpServletRequest request
    ) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.unauthorized();
            }

            // Get user information from token
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(token);
            Integer role = claims.get("role", Integer.class);
            Integer userId = claims.get("userId", Integer.class);
            
            // Verify role is restaurant manager
            if (role == null || role != 1) {
                return Result.forbidden("No permission to access, this interface is for restaurant managers only");
            }

            Map<String, Object> result = reservationService.getManagerReservations(page, pageSize, status, restaurantId, userId);
            return Result.success(result, "Retrieved successfully");
        } catch (Exception e) {
            logger.error("Failed to get reservation list: ", e);
            return Result.error(500, "Failed to get reservation list: " + e.getMessage());
        }
    }
    
    /**
     * Confirm reservation (Restaurant Manager only)
     * @param reservationId Reservation ID
     * @param request Request object for getting token
     * @return Operation result
     */
    @PutMapping("/reservations/{reservationId}/confirm")
    public Result<?> confirmReservation(
            @PathVariable Integer reservationId,
            HttpServletRequest request
    ) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.unauthorized();
            }

            // Get user information from token
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(token);
            Integer role = claims.get("role", Integer.class);
            Integer userId = claims.get("userId", Integer.class);


            return reservationService.confirmReservation(reservationId, userId, role);
        } catch (Exception e) {
            logger.error("Failed to confirm reservation: ", e);
            return Result.error(500, "Failed to confirm reservation: " + e.getMessage());
        }
    }
    
    /**
     * Reject reservation (Restaurant Manager only)
     * @param reservationId Reservation ID
     * @param request Request object for getting token
     * @return Operation result
     */
    @PutMapping("/reservations/{reservationId}/reject")
    public Result<?> rejectReservation(
            @PathVariable Integer reservationId,
            @RequestBody Map<String, Object> map,
            HttpServletRequest request
    ) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.unauthorized();
            }

            // Get user information from token
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(token);
            Integer role = claims.get("role", Integer.class);
            Integer userId = claims.get("userId", Integer.class);

            String reason = (String) map.get("reason");
            System.out.println(map);
            System.out.println("reason=" + reason);
            return reservationService.rejectReservation(reservationId, reason, userId, role);
        } catch (Exception e) {
            logger.error("Failed to reject reservation: ", e);
            return Result.error(500, "Failed to reject reservation: " + e.getMessage());
        }
    }
    
    /**
     * Complete reservation (Restaurant Manager only)
     * @param reservationId Reservation ID
     * @param request Request object for getting token
     * @return Operation result
     */
    @PutMapping("/reservations/{reservationId}/complete")
    public Result<?> completeReservation(
            @PathVariable Integer reservationId,
            HttpServletRequest request
    ) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.unauthorized();
            }

            // Get user information from token
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(token);
            Integer role = claims.get("role", Integer.class);
            Integer userId = claims.get("userId", Integer.class);


            return reservationService.completeReservation(reservationId, userId, role);
        } catch (Exception e) {
            logger.error("Failed to complete reservation: ", e);
            return Result.error(500, "Failed to complete reservation: " + e.getMessage());
        }
    }
} 