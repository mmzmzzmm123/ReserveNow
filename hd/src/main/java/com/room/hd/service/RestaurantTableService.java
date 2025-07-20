package com.room.hd.service;

import com.room.hd.common.Result;
import com.room.hd.entity.RestaurantTable;

import java.util.Map;

/**
 * Restaurant Table Management Service Interface
 */
public interface RestaurantTableService {
    
    /**
     * Get all table list (for administrator)
     * @param page Page number
     * @param pageSize Items per page
     * @param restaurantId Restaurant ID (optional)
     * @return Table list
     */
    Result<Map<String, Object>> getAllTables(Integer page, Integer pageSize, Integer restaurantId);
    
    /**
     * Get table list for specific restaurant (for restaurant manager)
     * @param page Page number
     * @param pageSize Items per page
     * @param restaurantId Restaurant ID
     * @param managerId Restaurant manager ID
     * @return Table list
     */
    Result<Map<String, Object>> getRestaurantTables(Integer page, Integer pageSize, Integer restaurantId, Integer managerId);
    
    /**
     * Get all tables under current restaurant manager
     * @param page Page number
     * @param pageSize Items per page
     * @param managerId Restaurant manager ID
     * @return Table list
     */
    Result<Map<String, Object>> getManagerAllTables(Integer page, Integer pageSize, Integer managerId);
    
    /**
     * Get table details
     * @param id Table ID
     * @return Table details
     */
    Result<RestaurantTable> getTableDetail(Integer id);
    
    /**
     * Add table
     * @param table Table information
     * @param userId Current user ID
     * @return Addition result
     */
    Result<RestaurantTable> addTable(RestaurantTable table, Integer userId);
    
    /**
     * Update table information
     * @param id Table ID
     * @param table Table information
     * @param userId Current user ID
     * @return Update result
     */
    Result<RestaurantTable> updateTable(Integer id, RestaurantTable table, Integer userId);
    
    /**
     * Delete table
     * @param id Table ID
     * @param userId Current user ID
     * @return Deletion result
     */
    Result<?> deleteTable(Integer id, Integer userId);
    
    /**
     * Check user's permission for restaurant
     * @param userId User ID
     * @param restaurantId Restaurant ID
     * @return Whether user has permission
     */
    Result<Boolean> checkRestaurantPermission(Integer userId, Integer restaurantId);
}