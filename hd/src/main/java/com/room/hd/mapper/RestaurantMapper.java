package com.room.hd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.room.hd.entity.Restaurant;
import com.room.hd.entity.RestaurantTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Restaurant Mapper Interface
 */
@Mapper
public interface RestaurantMapper extends BaseMapper<Restaurant> {

    /**
     * Get restaurant's average rating
     * @param restaurantId Restaurant ID
     * @return Average rating
     */
    @Select("SELECT COALESCE(AVG(rating), 0) FROM reviews WHERE restaurant_id = #{restaurantId}")
    Double getAverageRating(@Param("restaurantId") Integer restaurantId);
    
    /**
     * Get restaurant's review count
     * @param restaurantId Restaurant ID
     * @return Review count
     */
    @Select("SELECT COUNT(*) FROM reviews WHERE restaurant_id = #{restaurantId}")
    Integer getReviewCount(@Param("restaurantId") Integer restaurantId);
    
    /**
     * Get restaurant's table information
     * @param restaurantId Restaurant ID
     * @return Restaurant table list
     */
    @Select("SELECT id, restaurant_id, type, capacity, created_at, updated_at FROM tables WHERE restaurant_id = #{restaurantId}")
    List<RestaurantTable> getRestaurantTables(@Param("restaurantId") Integer restaurantId);
    
    /**
     * Count total number of all restaurants
     */
    @Select("SELECT COUNT(*) FROM restaurants")
    Long countTotalRestaurants();
    
    /**
     * Count restaurant manager's total restaurants
     */
    @Select("SELECT COUNT(*) FROM restaurants WHERE owner_id = #{userId}")
    Long countManagerRestaurants(@Param("userId") Integer userId);
    
    /**
     * Count daily restaurant numbers
     */
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count FROM restaurants WHERE created_at BETWEEN #{startDate} AND #{endDate} GROUP BY DATE(created_at)")
    List<Map<String, Object>> countDailyRestaurants(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    /**
     * Count restaurant manager's daily restaurant numbers
     */
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count FROM restaurants WHERE owner_id = #{userId} AND created_at BETWEEN #{startDate} AND #{endDate} GROUP BY DATE(created_at)")
    List<Map<String, Object>> countManagerDailyRestaurants(@Param("userId") Integer userId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    /**
     * Count restaurants by status
     */
    @Select("SELECT status, COUNT(*) as count FROM restaurants GROUP BY status")
    List<Map<String, Object>> countRestaurantsByStatus();
    
    /**
     * Count restaurant manager's restaurants by status
     */
    @Select("SELECT status, COUNT(*) as count FROM restaurants WHERE owner_id = #{userId} GROUP BY status")
    List<Map<String, Object>> countManagerRestaurantsByStatus(@Param("userId") Integer userId);
    
    /**
     * Count daily restaurants by status
     */
    @Select("SELECT DATE(created_at) as date, status, COUNT(*) as count FROM restaurants WHERE created_at BETWEEN #{startDate} AND #{endDate} GROUP BY DATE(created_at), status")
    List<Map<String, Object>> countDailyRestaurantsByStatus(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    /**
     * Count restaurant manager's daily restaurants by status
     */
    @Select("SELECT DATE(created_at) as date, status, COUNT(*) as count FROM restaurants WHERE owner_id = #{userId} AND created_at BETWEEN #{startDate} AND #{endDate} GROUP BY DATE(created_at), status")
    List<Map<String, Object>> countManagerDailyRestaurantsByStatus(@Param("userId") Integer userId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    /**
     * Get list of restaurant IDs that staff belongs to
     * @param staffId Staff ID
     * @return List of restaurant IDs
     */
    @Select("SELECT DISTINCT r.id FROM restaurants r " +
            "JOIN staff s ON r.owner_id = s.manager_id " +
            "WHERE s.user_id = #{staffId} AND s.status = 1")
    List<Integer> getRestaurantIdsByStaffId(@Param("staffId") Integer staffId);
} 