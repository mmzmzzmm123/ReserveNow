package com.room.hd.service;

import com.room.hd.common.Result;
import com.room.hd.entity.Review;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * Review Service Interface
 */
public interface ReviewService extends IService<Review> {

    /**
     * Get restaurant review list
     * @param restaurantId Restaurant ID
     * @param page Current page number
     * @param pageSize Number of items per page
     * @param rating Rating filter
     * @return Review list
     */
    Result<Map<String, Object>> getRestaurantReviews(Integer restaurantId, Integer page, Integer pageSize, Integer rating);

    /**
     * Add review
     * @param review Review entity
     * @return Add result
     */
    Result<Review> addReview(Review review);

    /**
     * Delete review
     * @param reviewId Review ID
     * @param userId User ID (can only delete own reviews)
     * @return Delete result
     */
    Result<Boolean> deleteReview(Integer reviewId, Integer userId);

    /**
     * Get review list for administrator
     * @param page Page number
     * @param pageSize Page size
     * @param restaurantId Restaurant ID (optional)
     * @param rating Rating (optional)
     * @return Review list and total count
     */
    Map<String, Object> getAdminReviewList(int page, int pageSize, Integer restaurantId, Integer rating);

    /**
     * Delete review
     * @param reviewId Review ID
     * @return Success status
     */
    boolean deleteReview(Integer reviewId);
} 