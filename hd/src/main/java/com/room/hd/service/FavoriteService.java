package com.room.hd.service;

import com.room.hd.common.Result;

import java.util.Map;

/**
 * Favorite Service Interface
 */
public interface FavoriteService {
    
    /**
     * Add to favorites
     * @param restaurantId Restaurant ID
     * @param userId User ID
     * @return Favorite result
     */
    Result<?> addFavorite(Integer restaurantId, Integer userId);
    
    /**
     * Remove from favorites
     * @param restaurantId Restaurant ID
     * @param userId User ID
     * @return Remove favorite result
     */
    Result<?> removeFavorite(Integer restaurantId, Integer userId);
    
    /**
     * Get favorite list
     * @param page Page number
     * @param pageSize Items per page
     * @param userId User ID
     * @return Favorite list
     */
    Result<?> getFavoriteList(Integer page, Integer pageSize, Integer userId);
    
    /**
     * Check if already favorited
     * @param userId User ID
     * @param restaurantId Restaurant ID
     * @return Whether already favorited
     */
    boolean isFavorite(Integer userId, Integer restaurantId);
} 