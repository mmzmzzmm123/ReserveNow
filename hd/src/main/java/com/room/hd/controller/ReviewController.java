package com.room.hd.controller;

import com.room.hd.common.Result;
import com.room.hd.entity.Review;
import com.room.hd.service.ReviewService;
import com.room.hd.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Review Controller
 */
@RestController
@CrossOrigin
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * Get restaurant review list
     * @param restaurantId Restaurant ID
     * @param page Current page number
     * @param pageSize Number of items per page
     * @param rating Rating filter
     * @return Review list
     */
    @GetMapping("/restaurants/{restaurantId}/reviews")
    public Result<Map<String, Object>> getRestaurantReviews(
            @PathVariable Integer restaurantId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer rating) {
        return reviewService.getRestaurantReviews(restaurantId, page, pageSize, rating);
    }

    /**
     * Add review
     * @param review Review information including restaurantId, content, photos, videos, rating and reservationId
     * @param request HTTP request
     * @return Add result
     */
    @PostMapping("/reviews")
    public Result<Review> addReview(@RequestBody Review review, HttpServletRequest request) {
        try {
            // Get token from request header
            String token = request.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                return Result.unauthorized();
            }

            // Parse token to get user ID
            Integer userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.unauthorized();
            }

            // Set user ID
            review.setUserId(userId);
            
            // 注意：reservationId字段会从请求体中自动映射到Review对象中
            // 无需额外处理，服务层会根据此ID更新预约表的reviewId

            return reviewService.addReview(review);
        } catch (Exception e) {
            return Result.error(500, "Failed to add review: " + e.getMessage());
        }
    }

    /**
     * Delete review
     * @param reviewId Review ID
     * @param request HTTP request
     * @return Delete result
     */
    @DeleteMapping("/reviews/{reviewId}")
    public Result<Boolean> deleteReview(@PathVariable Integer reviewId, HttpServletRequest request) {
        try {
            // Get token from request header
            String token = request.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                return Result.unauthorized();
            }

            // Parse token to get user ID
            Integer userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.unauthorized();
            }

            return reviewService.deleteReview(reviewId, userId);
        } catch (Exception e) {
            return Result.error(500, "Failed to delete review: " + e.getMessage());
        }
    }
} 