package com.room.hd.service;

import com.room.hd.common.Result;
import com.room.hd.vo.RestaurantVO;

import java.util.Map;

/**
 * Restaurant Management Service Interface
 */
public interface RestaurantManageService {
    
    /**
     * Get restaurant list
     * @param page Page number
     * @param pageSize Items per page
     * @param keyword Search keyword
     * @param status Status filter
     * @return Restaurant list
     */
    Result<Map<String, Object>> getRestaurantList(Integer page, Integer pageSize, String keyword, Integer status);
    
    /**
     * Get restaurant details
     * @param id Restaurant ID
     * @return Restaurant details
     */
    Result<RestaurantVO> getRestaurantDetail(Integer id);
    
    /**
     * Add restaurant
     * @param restaurantVO Restaurant information
     * @param userId Current user ID
     * @return Addition result
     */
    Result<RestaurantVO> addRestaurant(RestaurantVO restaurantVO, Integer userId);
    
    /**
     * Update restaurant information
     * @param id Restaurant ID
     * @param restaurantVO Restaurant information
     * @param userId Current user ID
     * @return Update result
     */
    Result<RestaurantVO> updateRestaurant(Integer id, RestaurantVO restaurantVO, Integer userId);
    
    /**
     * Delete restaurant
     * @param id Restaurant ID
     * @param userId Current user ID
     * @return Deletion result
     */
    Result<?> deleteRestaurant(Integer id, Integer userId);
    
    /**
     * Update restaurant status
     * @param id Restaurant ID
     * @param status Status value
     * @param userId Current user ID
     * @return Update result
     */
    Result<?> updateRestaurantStatus(Integer id, Integer status, Integer userId);
    
    /**
     * Check user permission
     * @param token JWT token
     * @return Check result
     */
    Result<Map<String, Object>> checkManagerRole(String token);
    
    /**
     * Get restaurant list for restaurant manager
     * @param page Page number
     * @param pageSize Items per page
     * @param keyword Search keyword
     * @param status Status filter
     * @param managerId Restaurant manager ID
     * @return Restaurant list
     */
    Result<Map<String, Object>> getManagerRestaurantList(Integer page, Integer pageSize, String keyword, Integer status, Integer managerId);
}