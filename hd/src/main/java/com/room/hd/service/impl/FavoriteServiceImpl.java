package com.room.hd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.room.hd.common.Result;
import com.room.hd.entity.Favorite;
import com.room.hd.entity.Restaurant;
import com.room.hd.mapper.FavoriteMapper;
import com.room.hd.mapper.RestaurantMapper;
import com.room.hd.service.FavoriteService;
import com.room.hd.vo.FavoriteRestaurantVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Favorite Service Implementation
 */
@Service
@Slf4j
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Resource
    private FavoriteMapper favoriteMapper;
    
    @Resource
    private RestaurantMapper restaurantMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> addFavorite(Integer restaurantId, Integer userId) {
        try {
            // Check parameters
            if (restaurantId == null) {
                return Result.validateFailed("Restaurant ID cannot be empty");
            }
            
            if (userId == null) {
                return Result.validateFailed("User ID cannot be empty");
            }
            
            // Check if restaurant exists
            Restaurant restaurant = restaurantMapper.selectById(restaurantId);
            if (restaurant == null) {
                return Result.validateFailed("Restaurant not found");
            }
            
            // Check if already favorited
            LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Favorite::getUserId, userId)
                      .eq(Favorite::getRestaurantId, restaurantId);
            Favorite existFavorite = favoriteMapper.selectOne(queryWrapper);
            
            if (existFavorite != null) {
                return Result.validateFailed("Already favorited");
            }
            
            // Create favorite record
            Favorite favorite = new Favorite();
            favorite.setRestaurantId(restaurantId);
            favorite.setUserId(userId);  // Ensure user ID is set
            favorite.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            
            // Insert favorite record
            favoriteMapper.insert(favorite);
            
            // Build return data
            Map<String, Object> data = new HashMap<>();
            data.put("restaurantId", restaurantId);
            data.put("createdAt", favorite.getCreatedAt());
            
            return Result.success(data, "Added to favorites successfully");
        } catch (Exception e) {
            log.error("Failed to add favorite", e);
            return Result.error(500, "Failed to add favorite: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> removeFavorite(Integer restaurantId, Integer userId) {
        try {
            // Check parameters
            if (restaurantId == null) {
                return Result.validateFailed("Restaurant ID cannot be empty");
            }
            
            if (userId == null) {
                return Result.validateFailed("User ID cannot be empty");
            }
            
            // Query favorite record
            LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Favorite::getUserId, userId)
                      .eq(Favorite::getRestaurantId, restaurantId);
            Favorite favorite = favoriteMapper.selectOne(queryWrapper);
            
            if (favorite == null) {
                return Result.validateFailed("Not favorited");
            }
            
            // Delete favorite record
            favoriteMapper.delete(queryWrapper);
            
            return Result.success(null, "Removed from favorites successfully");
        } catch (Exception e) {
            log.error("Failed to remove favorite", e);
            return Result.error(500, "Failed to remove favorite: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Map<String, Object>> getFavoriteList(Integer page, Integer pageSize, Integer userId) {
        try {
            // Check parameters
            if (userId == null) {
                return Result.validateFailed("User ID cannot be empty");
            }
            
            // Set default values
            page = page == null ? 1 : page;
            pageSize = pageSize == null ? 10 : pageSize;
            
            // Query favorite records with pagination
            Page<Favorite> favoritePageInfo = new Page<>(page, pageSize);
            LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Favorite::getUserId, userId)
                      .orderByDesc(Favorite::getCreatedAt);
            
            Page<Favorite> favoritePage = favoriteMapper.selectPage(favoritePageInfo, queryWrapper);
            
            // Query restaurant details for favorites
            List<FavoriteRestaurantVO> favoriteVOList = favoritePage.getRecords().stream().map(favorite -> {
                Restaurant restaurant = restaurantMapper.selectById(favorite.getRestaurantId());
                if (restaurant != null) {
                    FavoriteRestaurantVO favoriteVO = convertToVO(restaurant, favorite);
                    return favoriteVO;
                }
                return null;
            }).collect(Collectors.toList());
            
            // Build return data
            Map<String, Object> result = new HashMap<>();
            result.put("total", favoritePage.getTotal());
            result.put("list", favoriteVOList);
            
            return Result.success(result, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get favorite list", e);
            return Result.error(500, "Failed to get favorite list: " + e.getMessage());
        }
    }
    
    /**
     * Check if already favorited
     * @param userId User ID
     * @param restaurantId Restaurant ID
     * @return Whether favorited
     */
    @Override
    public boolean isFavorite(Integer userId, Integer restaurantId) {
        if (userId == null || restaurantId == null) {
            return false;
        }
        
        // Use checkFavorite method defined in mapper
        int count = favoriteMapper.checkFavorite(userId, restaurantId);
        return count > 0;
    }
    
    /**
     * Convert Restaurant and Favorite to FavoriteRestaurantVO
     * @param restaurant Restaurant entity
     * @param favorite Favorite entity
     * @return Favorite restaurant VO
     */
    private FavoriteRestaurantVO convertToVO(Restaurant restaurant, Favorite favorite) {
        if (restaurant == null || favorite == null) {
            return null;
        }
        
        FavoriteRestaurantVO favoriteVO = new FavoriteRestaurantVO();
        favoriteVO.setRestaurantId(restaurant.getId());
        favoriteVO.setRestaurantName(restaurant.getName());
        favoriteVO.setDescription(restaurant.getDescription());
        favoriteVO.setAddress(restaurant.getAddress());
        favoriteVO.setCuisine(restaurant.getCuisine());
        
        // Process image URLs
        if (StringUtils.hasText(restaurant.getPhotos())) {
            List<String> photoList = Arrays.asList(restaurant.getPhotos().split("\\|"));
            favoriteVO.setPhotos(photoList);
        } else {
            favoriteVO.setPhotos(new ArrayList<>());
        }
        
        // Get restaurant rating
        Double rating = restaurantMapper.getAverageRating(restaurant.getId());
        favoriteVO.setRating(rating);
        
        // Set favorite time
        favoriteVO.setCreatedAt(favorite.getCreatedAt());
        
        return favoriteVO;
    }
} 