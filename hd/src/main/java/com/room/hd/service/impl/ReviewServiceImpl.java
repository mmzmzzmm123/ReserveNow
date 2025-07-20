package com.room.hd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.room.hd.common.Result;
import com.room.hd.entity.Reservation;
import com.room.hd.entity.Review;
import com.room.hd.entity.User;
import com.room.hd.mapper.ReservationMapper;
import com.room.hd.mapper.ReviewMapper;
import com.room.hd.mapper.UserMapper;
import com.room.hd.service.ReviewService;
import com.room.hd.vo.ReviewVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Review Service Implementation
 */
@Service
@Slf4j
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Resource
    private ReviewMapper reviewMapper;

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private ReservationMapper reservationMapper;

    @Override
    public Result<Map<String, Object>> getRestaurantReviews(Integer restaurantId, Integer page, Integer pageSize, Integer rating) {
        try {
            if (restaurantId == null) {
                return Result.validateFailed("Restaurant ID cannot be empty");
            }

            // Set default values
            if (page == null || page < 1) {
                page = 1;
            }

            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }

            // Build query conditions
            LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Review::getRestaurantId, restaurantId);

            // Filter by rating if specified
            if (rating != null && rating > 0 && rating <= 5) {
                queryWrapper.eq(Review::getRating, rating);
            }

            // Sort by creation time in descending order
            queryWrapper.orderByDesc(Review::getCreatedAt);

            // Pagination query
            IPage<Review> pageResult = reviewMapper.selectPage(new Page<>(page, pageSize), queryWrapper);

            // Convert query results to VO objects
            List<ReviewVO> reviewVOList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // Return results
            Map<String, Object> result = new HashMap<>();
            result.put("total", pageResult.getTotal());
            result.put("list", reviewVOList);

            return Result.success(result, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get restaurant review list", e);
            return Result.error(500, "Failed to get restaurant review list: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Review> addReview(Review review) {
        try {
            if (review == null) {
                return Result.validateFailed("Review information cannot be empty");
            }

            if (review.getRestaurantId() == null) {
                return Result.validateFailed("Restaurant ID cannot be empty");
            }

            if (review.getUserId() == null) {
                return Result.validateFailed("User ID cannot be empty");
            }

            if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
                return Result.validateFailed("Rating must be between 1 and 5");
            }

            // Set creation time
            review.setCreatedAt(java.time.OffsetDateTime.now());
            review.setUpdatedAt(review.getCreatedAt());

            Integer reservationId = Integer.valueOf(review.getId());
            // Save review
            UUID uuid = UUID.randomUUID();
            review.setId(uuid.toString());
            reviewMapper.insert(review);
            
            // 如果包含预约ID，更新预约表中的reviewId字段
            System.out.println("reservationId"+reservationId);
            if (reservationId != null) {
                Reservation reservation = reservationMapper.selectById(reservationId);
                if (reservation != null) {
                    reservation.setReviewId(uuid.toString());
                    reservationMapper.updateById(reservation);
                    log.info("Updated reservation {} with review ID {}", reservationId, review.getId());
                } else {
                    log.warn("Reservation not found: {}", reservationId);
                }
            }

            return Result.success(review, "Review submitted successfully");
        } catch (Exception e) {
            log.error("Failed to add review", e);
            return Result.error(500, "Failed to add review: " + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> deleteReview(Integer reviewId, Integer userId) {
        try {
            if (reviewId == null) {
                return Result.validateFailed("Review ID cannot be empty");
            }

            if (userId == null) {
                return Result.validateFailed("User ID cannot be empty");
            }

            // Query review
            Review review = reviewMapper.selectById(reviewId);
            if (review == null) {
                return Result.notFound();
            }

            // Check if it's the current user's review
            if (!review.getUserId().equals(userId)) {
                return Result.forbidden();
            }

            // Delete review
            reviewMapper.deleteById(reviewId);

            return Result.success(true, "Deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete review", e);
            return Result.error(500, "Failed to delete review: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getAdminReviewList(int page, int pageSize, Integer restaurantId, Integer rating) {
        try {
            // Calculate offset
            int offset = (page - 1) * pageSize;

            // Get review list
            List<Review> reviews = reviewMapper.getAdminReviewList(offset, pageSize, restaurantId, rating);

            // Get total count
            Long total = reviewMapper.countAdminReviews(restaurantId, rating);

            // Return results
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", reviews);

            return result;
        } catch (Exception e) {
            log.error("Failed to get admin review list: ", e);
            throw new RuntimeException("Failed to get review list: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteReview(Integer reviewId) {
        try {
            // Check if review exists
            Review review = reviewMapper.selectById(reviewId);
            if (review == null) {
                log.warn("Review to delete does not exist, reviewId: {}", reviewId);
                return false;
            }

            // Delete review
            int result = reviewMapper.deleteById(reviewId);
            return result > 0;
        } catch (Exception e) {
            log.error("Failed to delete review: ", e);
            throw new RuntimeException("Failed to delete review: " + e.getMessage());
        }
    }

    /**
     * Convert Review entity to ReviewVO
     * @param review Review entity
     * @return ReviewVO
     */
    private ReviewVO convertToVO(Review review) {
        if (review == null) {
            return null;
        }

        ReviewVO reviewVO = new ReviewVO();
        BeanUtils.copyProperties(review, reviewVO);

        // Process photo URLs
        if (StringUtils.hasText(review.getPhotos())) {
            List<String> photoList = Arrays.asList(review.getPhotos().split("\\|"));
            reviewVO.setPhotos(photoList);
        } else {
            reviewVO.setPhotos(new ArrayList<>());
        }

        // Process video URLs
        if (StringUtils.hasText(review.getVideos())) {
            List<String> videoList = Arrays.asList(review.getVideos().split("\\|"));
            reviewVO.setVideos(videoList);
        } else {
            reviewVO.setVideos(new ArrayList<>());
        }

        // Get user information
        User user = userMapper.selectById(review.getUserId());
        if (user != null) {
            reviewVO.setUserName(user.getName());
            reviewVO.setUserAvatar(user.getAvatar());
        }

        return reviewVO;
    }
}