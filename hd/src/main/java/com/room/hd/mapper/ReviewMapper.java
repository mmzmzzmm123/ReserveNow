package com.room.hd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.room.hd.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Review Mapper Interface
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    
    /**
     * Get review list for administrator
     */
    @Select("<script>SELECT r.*, u.name as user_name, u.avatar as user_avatar, res.name as restaurant_name " +
           "FROM reviews r " +
           "LEFT JOIN users u ON r.user_id = u.id " +
           "LEFT JOIN restaurants res ON r.restaurant_id = res.id " +
           "<where> 1=1 " +
           "<if test='restaurantId != null'> AND r.restaurant_id = #{restaurantId} </if>" +
           "<if test='rating != null'> AND r.rating = #{rating} </if>" +
           "</where> " +
           "ORDER BY r.created_at DESC LIMIT #{pageSize} OFFSET #{offset}</script>")
    List<Review> getAdminReviewList(@Param("offset") int offset, @Param("pageSize") int pageSize, 
                                   @Param("restaurantId") Integer restaurantId, @Param("rating") Integer rating);
    
    /**
     * Get total number of reviews
     */
    @Select("<script>SELECT COUNT(*) FROM reviews r " +
           "<where> 1=1 " +
           "<if test='restaurantId != null'> AND r.restaurant_id = #{restaurantId} </if>" +
           "<if test='rating != null'> AND r.rating = #{rating} </if>" +
           "</where></script>")
    Long countAdminReviews(@Param("restaurantId") Integer restaurantId, @Param("rating") Integer rating);
    
    /**
     * Check if user has reviewed a reservation
     * @param userId User ID
     * @param restaurantId Restaurant ID
     * @return Number of reviews
     */
    @Select("SELECT COUNT(*) FROM reviews WHERE user_id = #{userId} AND restaurant_id = #{restaurantId}")
    Integer checkReviewExistence(@Param("userId") Integer userId, @Param("restaurantId") Integer restaurantId);
} 