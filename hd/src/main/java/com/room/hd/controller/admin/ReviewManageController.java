package com.room.hd.controller.admin;

import com.room.hd.common.Result;
import com.room.hd.service.ReviewService;
import com.room.hd.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Review Management Controller
 */
@Slf4j
@RestController
@RequestMapping("/admin/reviews")
@CrossOrigin
public class ReviewManageController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * Get review list
     */
    @GetMapping
    public Result<?> getReviewList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer restaurantId,
            @RequestParam(required = false) Integer rating) {
        try {
            // Verify if user is administrator
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(token.replace("Bearer ", ""));
            Integer roleValue = claims.get("role", Integer.class);
            if (roleValue != 0) {
                return Result.error(500,"No permission to access");
            }

            Map<String, Object> result = reviewService.getAdminReviewList(page, pageSize, restaurantId, rating);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to get review list: ", e);
            return Result.error(500,"Failed to get review list: " + e.getMessage());
        }
    }

    /**
     * Delete review
     */
    @DeleteMapping("/{reviewId}")
    public Result<?> deleteReview(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer reviewId) {
        try {
            // Verify if user is administrator
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(token.replace("Bearer ", ""));
            Integer roleValue = claims.get("role", Integer.class);
            if (roleValue != 0) {
                return Result.error(500,"No permission to access");
            }

            boolean success = reviewService.deleteReview(reviewId);
            return success ? Result.success("") : Result.error(500,"Delete failed");
        } catch (Exception e) {
            log.error("Failed to delete review: ", e);
            return Result.error(500,"Failed to delete review: " + e.getMessage());
        }
    }
} 