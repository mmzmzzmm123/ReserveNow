package com.room.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.room.backend.common.Result;
import com.room.backend.entity.Restaurant;
import com.room.backend.mapper.RestaurantMapper;
import com.room.backend.mapper.UserMapper;
import com.room.backend.service.FavoriteService;
import com.room.backend.service.RestaurantService;
import com.room.backend.vo.RestaurantVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Restaurant Service Implementation
 */
@Service
@Slf4j
public class RestaurantServiceImpl extends ServiceImpl<RestaurantMapper, Restaurant> implements RestaurantService {

    @Resource
    private RestaurantMapper restaurantMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private FavoriteService favoriteService;

    // Restaurant status constants
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_CLOSED = 2;
    private static final int STATUS_OPERATING = 3;

    @Override
    public Result<Map<String, Object>> getRestaurantList(Integer page, Integer pageSize, Integer status, String keyword) {
        return getRestaurantList(page, pageSize, status, keyword, null);
    }

    /**
     * Get restaurant list with cuisine filter
     * @param page Current page number
     * @param pageSize Number of items per page
     * @param status Restaurant status
     * @param keyword Search keyword
     * @param cuisine Cuisine type
     * @return Restaurant list
     */
    public Result<Map<String, Object>> getRestaurantList(Integer page, Integer pageSize, Integer status, String keyword, String cuisine) {
        // Default value handling
        page = page == null || page < 1 ? 1 : page;
        pageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        // Build query conditions
        LambdaQueryWrapper<Restaurant> queryWrapper = new LambdaQueryWrapper<>();

        // Status condition
        if (status != null) {
            queryWrapper.eq(Restaurant::getStatus, status);
        }

        // Cuisine filter
        if (StringUtils.hasText(cuisine)) {
            queryWrapper.eq(Restaurant::getCuisine, cuisine);
        }

        // Keyword search
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper ->
                    wrapper.like(Restaurant::getName, keyword)
                            .or()
                            .like(Restaurant::getAddress, keyword)
                            .or()
                            .like(Restaurant::getDescription, keyword)
            );
        }

        // Pagination query
        Page<Restaurant> pageResult = restaurantMapper.selectPage(new Page<>(page, pageSize), queryWrapper);

        // Build return results
        List<RestaurantVO> restaurantVOList = new ArrayList<>();
        for (Restaurant restaurant : pageResult.getRecords()) {
            RestaurantVO restaurantVO = convertToVO(restaurant);
            restaurantVOList.add(restaurantVO);
        }

        // Assemble pagination results
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageResult.getTotal());
        result.put("list", restaurantVOList);

        return Result.success(result, "Retrieved successfully");
    }

    /**
     * Convert Restaurant entity to RestaurantVO
     * @param restaurant Restaurant entity
     * @return RestaurantVO
     */
    private RestaurantVO convertToVO(Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }

        RestaurantVO restaurantVO = new RestaurantVO();
        BeanUtils.copyProperties(restaurant, restaurantVO);

        // Process photo URLs
        if (StringUtils.hasText(restaurant.getPhotos())) {
            List<String> photoList = Arrays.asList(restaurant.getPhotos().split("\\|"));
            restaurantVO.setPhotos(photoList);
        } else {
            restaurantVO.setPhotos(new ArrayList<>());
        }

        // Set coordinates
        restaurantVO.setLongitude(Double.valueOf(restaurant.getLongitude()));
        restaurantVO.setLatitude(Double.valueOf(restaurant.getLatitude()));
        // Get restaurant status name
        restaurantVO.setStatus(getStatusName(restaurant.getStatus()));

        // Get restaurant rating
        Double rating = restaurantMapper.getAverageRating(restaurant.getId());
        restaurantVO.setRating(rating);

        // Get review count
        Integer reviewCount = restaurantMapper.getReviewCount(restaurant.getId());
        restaurantVO.setReviewCount(reviewCount);

        // Get owner name
        if (restaurant.getOwnerId() != null) {
            String ownerName = userMapper.selectById(restaurant.getOwnerId()).getName();
            restaurantVO.setOwnerName(ownerName);
        }

        // Default not favorite
        restaurantVO.setIsFavorite(false);

        return restaurantVO;
    }

    /**
     * Get restaurant status name
     * @param status Status code
     * @return Status name
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "Unknown";
        }

        switch (status) {
            case STATUS_PENDING:
                return "Pending Review";
            case STATUS_APPROVED:
                return "Approved";
            case STATUS_CLOSED:
                return "Closed";
            case STATUS_OPERATING:
                return "Operating";
            default:
                return "Unknown";
        }
    }

    /**
     * Get restaurant list with favorite status
     * @param page Current page number
     * @param pageSize Number of items per page
     * @param status Restaurant status
     * @param keyword Search keyword
     * @param userId User ID for checking favorite status
     * @return Restaurant list
     */
    public Result<Map<String, Object>> getRestaurantListWithFavorite(Integer page, Integer pageSize, Integer status, String keyword, Integer userId) {
        return getRestaurantListWithFavorite(page, pageSize, status, keyword, null, userId);
    }

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
    public Result<Map<String, Object>> getRestaurantListWithFavorite(Integer page, Integer pageSize, Integer status, String keyword, String cuisine, Integer userId) {
        Result<Map<String, Object>> result = getRestaurantList(page, pageSize, status, keyword, cuisine);

        // Set favorite status if user ID is not empty
        if (userId != null && result.getCode() == 200 && result.getData() != null) {
            Map<String, Object> data = result.getData();
            List<RestaurantVO> list = (List<RestaurantVO>) data.get("list");

            for (RestaurantVO restaurantVO : list) {
                // Set favorite status
                boolean isFavorite = favoriteService.isFavorite(userId, restaurantVO.getId());
                restaurantVO.setIsFavorite(isFavorite);
            }
        }

        return result;
    }

    @Override
    public Result<List<String>> getCuisineList() {
        // Get all cuisine list
        List<String> cuisineList = restaurantMapper.selectList(null)
                .stream()
                .map(Restaurant::getCuisine)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());

        return Result.success(cuisineList, "Retrieved successfully");
    }

    /**
     * Get restaurant details
     * @param id Restaurant ID
     * @return Restaurant details
     */
    @Override
    public Result<RestaurantVO> getRestaurantDetail(Integer id) {
        try {
            if (id == null) {
                return Result.validateFailed("Restaurant ID cannot be empty");
            }

            // Get restaurant information
            Restaurant restaurant = restaurantMapper.selectById(id);
            if (restaurant == null) {
                return Result.notFound();
            }

            // Convert to VO
            RestaurantVO restaurantVO = convertToVO(restaurant);

            // Get restaurant table information
            restaurantVO.setTables(restaurantMapper.getRestaurantTables(id));

            return Result.success(restaurantVO, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to retrieve restaurant details", e);
            return Result.error(500, "Failed to retrieve restaurant details: " + e.getMessage());
        }
    }

    /**
     * Get restaurant details with favorite status
     * @param id Restaurant ID
     * @param userId User ID for checking favorite status
     * @return Restaurant details, including favorite status
     */
    @Override
    public Result<RestaurantVO> getRestaurantDetailWithFavorite(Integer id, Integer userId) {
        try {
            if (id == null) {
                return Result.validateFailed("Restaurant ID cannot be empty");
            }

            if (userId == null) {
                return Result.validateFailed("User ID cannot be empty");
            }

            // Get restaurant information
            Restaurant restaurant = restaurantMapper.selectById(id);
            System.out.println("restaurant = " + restaurant);
            if (restaurant == null) {
                return Result.notFound();
            }

            // Convert to VO
            RestaurantVO restaurantVO = convertToVO(restaurant);

            // Get restaurant table information
            restaurantVO.setTables(restaurantMapper.getRestaurantTables(id));

            // Check if it's favorite
            boolean isFavorite = favoriteService.isFavorite(userId, id);
            restaurantVO.setIsFavorite(isFavorite);

            return Result.success(restaurantVO, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to retrieve restaurant details", e);
            return Result.error(500, "Failed to retrieve restaurant details: " + e.getMessage());
        }
    }
} 