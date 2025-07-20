package com.room.hd.service;

import com.room.hd.common.Result;
import com.room.hd.vo.RestaurantVO;

import java.util.List;
import java.util.Map;

/**
 * Restaurant Service Interface
 */
public interface RestaurantService {

    /**
     * Get restaurant list
     * @param page Current page number
     * @param pageSize Number of items per page
     * @param status Restaurant status
     * @param keyword Search keyword
     * @return Restaurant list
     */
    Result<Map<String, Object>> getRestaurantList(Integer page, Integer pageSize, Integer status, String keyword);

    /**
     * Get restaurant list with cuisine filter
     * @param page Current page number
     * @param pageSize Number of items per page
     * @param status Restaurant status
     * @param keyword Search keyword
     * @param cuisine Cuisine type
     * @return Restaurant list
     */
    Result<Map<String, Object>> getRestaurantList(Integer page, Integer pageSize, Integer status, String keyword, String cuisine);

    /**
     * Get restaurant list with favorite status
     * @param page Current page number
     * @param pageSize Number of items per page
     * @param status Restaurant status
     * @param keyword Search keyword
     * @param userId User ID for checking favorite status
     * @return Restaurant list
     */
    Result<Map<String, Object>> getRestaurantListWithFavorite(Integer page, Integer pageSize, Integer status, String keyword, Integer userId);

    /**
     * Get restaurant list with favorite status and cuisine filter
     * @param page Current page number
     * @param pageSize Number of items per page
     * @param status Restaurant status
     * @param keyword Search keyword
     * @param cuisine Cuisine type
     * @param userId User ID for checking favorite status
     * @return Restaurant list
     */
    Result<Map<String, Object>> getRestaurantListWithFavorite(Integer page, Integer pageSize, Integer status, String keyword, String cuisine, Integer userId);

    /**
     * Get cuisine list
     * @return Cuisine list
     */
    Result<List<String>> getCuisineList();

    /**
     * Get restaurant details
     * @param id Restaurant ID
     * @return Restaurant details
     */
    Result<RestaurantVO> getRestaurantDetail(Integer id);

    /**
     * Get restaurant details with favorite status
     * @param id Restaurant ID
     * @param userId User ID for checking favorite status
     * @return Restaurant details with favorite status
     */
    Result<RestaurantVO> getRestaurantDetailWithFavorite(Integer id, Integer userId);
} 