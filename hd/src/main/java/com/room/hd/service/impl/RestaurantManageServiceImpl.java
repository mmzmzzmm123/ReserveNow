package com.room.hd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.room.hd.common.Result;
import com.room.hd.entity.Restaurant;
import com.room.hd.entity.User;
import com.room.hd.mapper.RestaurantMapper;
import com.room.hd.mapper.UserMapper;
import com.room.hd.service.RestaurantManageService;
import com.room.hd.util.JwtTokenUtil;
import com.room.hd.vo.RestaurantVO;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
 * Restaurant Management Service Implementation
 */
@Service
@Slf4j
public class RestaurantManageServiceImpl implements RestaurantManageService {

    @Resource
    private RestaurantMapper restaurantMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Override
    public Result<Map<String, Object>> getRestaurantList(Integer page, Integer pageSize, String keyword, Integer status) {
        try {
            // Parameter validation
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }
            
            // Calculate offset
            int offset = (page - 1) * pageSize;
            
            // Build query conditions
            LambdaQueryWrapper<Restaurant> queryWrapper = new LambdaQueryWrapper<>();
            
            // Keyword search
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.like(Restaurant::getName, keyword)
                        .or()
                        .like(Restaurant::getAddress, keyword);
            }
            
            // Status filter
            if (status != null) {
                queryWrapper.eq(Restaurant::getStatus, status);
            }
            
            // Query total count
            long total = restaurantMapper.selectCount(queryWrapper);
            
            // Sort
            queryWrapper.orderByDesc(Restaurant::getId);
            
            // Pagination
            queryWrapper.last("LIMIT " + pageSize + " OFFSET " + offset);
            
            // Query list
            List<Restaurant> restaurantList = restaurantMapper.selectList(queryWrapper);
            
            // Convert to VO objects
            List<RestaurantVO> restaurantVOList = new ArrayList<>();
            for (Restaurant restaurant : restaurantList) {
                RestaurantVO restaurantVO = convertToVO(restaurant);
                
                // Query restaurant owner information
                if (restaurant.getOwnerId() != null) {
                    User owner = userMapper.selectById(restaurant.getOwnerId());
                    if (owner != null) {
                        restaurantVO.setOwnerName(owner.getName());
                    }
                }
                
                restaurantVOList.add(restaurantVO);
            }
            
            // Build return result
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", restaurantVOList);
            
            return Result.success(result, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get restaurant list", e);
            return Result.error(500, "Failed to get restaurant list: " + e.getMessage());
        }
    }
    
    @Override
    public Result<RestaurantVO> getRestaurantDetail(Integer id) {
        try {
            // Parameter validation
            if (id == null) {
                return Result.validateFailed("Restaurant ID cannot be empty");
            }
            
            // Query restaurant
            Restaurant restaurant = restaurantMapper.selectById(id);
            if (restaurant == null) {
                return Result.validateFailed("Restaurant not found");
            }
            
            // Convert to VO object
            RestaurantVO restaurantVO = convertToVO(restaurant);
            
            // Query restaurant owner information
            if (restaurant.getOwnerId() != null) {
                User owner = userMapper.selectById(restaurant.getOwnerId());
                if (owner != null) {
                    restaurantVO.setOwnerName(owner.getName());
                }
            }
            
            return Result.success(restaurantVO, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get restaurant details", e);
            return Result.error(500, "Failed to get restaurant details: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RestaurantVO> addRestaurant(RestaurantVO restaurantVO, Integer userId) {
        try {
            // Parameter validation
            if (StringUtils.isEmpty(restaurantVO.getName()) || StringUtils.isEmpty(restaurantVO.getAddress())) {
                return Result.validateFailed("Restaurant name and address cannot be empty");
            }
            
            // Check if user exists
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.validateFailed("User not found");
            }
            
            // Create restaurant entity
            Restaurant restaurant = new Restaurant();
            
            // Set basic information
            restaurant.setName(restaurantVO.getName());
            restaurant.setDescription(restaurantVO.getDescription());
            restaurant.setAddress(restaurantVO.getAddress());
            restaurant.setPhone(restaurantVO.getPhone());
            restaurant.setBusinessLicense(restaurantVO.getBusinessLicense());
            restaurant.setBusinessHours(restaurantVO.getBusinessHours());
            restaurant.setCuisine(restaurantVO.getCuisine());
            
            // Set coordinates
            if (restaurantVO.getLongitude() != null) {
                restaurant.setLongitude(restaurantVO.getLongitude());
            }
            if (restaurantVO.getLatitude() != null) {
                restaurant.setLatitude(restaurantVO.getLatitude());
            }
            
            // Process photos
            if (restaurantVO.getPhotos() != null && !restaurantVO.getPhotos().isEmpty()) {
                String photosStr = String.join("|", restaurantVO.getPhotos());
                restaurant.setPhotos(photosStr);
            }
            
            // Set status, new restaurants default to pending review
            restaurant.setStatus(0);
            
            // Set owner
            restaurant.setOwnerId(userId);
            
            // Set creation and update time
            restaurant.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            restaurant.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            
            // Save restaurant
            restaurantMapper.insert(restaurant);
            
            // Convert to VO object
            RestaurantVO result = convertToVO(restaurant);
            result.setOwnerName(user.getName());
            
            return Result.success(result, "Added successfully");
        } catch (Exception e) {
            log.error("Failed to add restaurant", e);
            return Result.error(500, "Failed to add restaurant: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RestaurantVO> updateRestaurant(Integer id, RestaurantVO restaurantVO, Integer userId) {
        try {
            // Parameter validation
            if (id == null) {
                return Result.validateFailed("Restaurant ID cannot be empty");
            }
            
            // Query restaurant
            Restaurant restaurant = restaurantMapper.selectById(id);
            if (restaurant == null) {
                return Result.validateFailed("Restaurant not found");
            }
            
            // Permission check: Only restaurant owner or admin can modify restaurant information
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.validateFailed("User not found");
            }
            
            // Admin(role=0) can modify any restaurant, restaurant manager(role=1) can only modify their own restaurants
            if (user.getRole() != 0 && !userId.equals(restaurant.getOwnerId())) {
                return Result.forbidden("No permission to modify restaurants created by others");
            }
            
            // Update restaurant basic information
            if (!StringUtils.isEmpty(restaurantVO.getName())) {
                restaurant.setName(restaurantVO.getName());
            }
            if (restaurantVO.getDescription() != null) {
                restaurant.setDescription(restaurantVO.getDescription());
            }
            if (!StringUtils.isEmpty(restaurantVO.getAddress())) {
                restaurant.setAddress(restaurantVO.getAddress());
            }
            if (!StringUtils.isEmpty(restaurantVO.getPhone())) {
                restaurant.setPhone(restaurantVO.getPhone());
            }
            if (!StringUtils.isEmpty(restaurantVO.getBusinessLicense())) {
                restaurant.setBusinessLicense(restaurantVO.getBusinessLicense());
            }
            if (!StringUtils.isEmpty(restaurantVO.getBusinessHours())) {
                restaurant.setBusinessHours(restaurantVO.getBusinessHours());
            }
            if (!StringUtils.isEmpty(restaurantVO.getCuisine())) {
                restaurant.setCuisine(restaurantVO.getCuisine());
            }
            
            // Update coordinates
            if (restaurantVO.getLongitude() != null) {
                restaurant.setLongitude(restaurantVO.getLongitude());
            }
            if (restaurantVO.getLatitude() != null) {
                restaurant.setLatitude(restaurantVO.getLatitude());
            }
            
            // Process photos
            if (restaurantVO.getPhotos() != null && !restaurantVO.getPhotos().isEmpty()) {
                String photosStr = String.join("|", restaurantVO.getPhotos());
                restaurant.setPhotos(photosStr);
            }
            
            // Update timestamp
            restaurant.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            
            // Save changes
            restaurantMapper.updateById(restaurant);
            
            // Convert to VO object
            RestaurantVO result = convertToVO(restaurant);
            
            // Set owner name
            User owner = userMapper.selectById(restaurant.getOwnerId());
            if (owner != null) {
                result.setOwnerName(owner.getName());
            }
            
            return Result.success(result, "Updated successfully");
        } catch (Exception e) {
            log.error("Failed to update restaurant", e);
            return Result.error(500, "Failed to update restaurant: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteRestaurant(Integer id, Integer userId) {
        try {
            // Parameter validation
            if (id == null) {
                return Result.validateFailed("Restaurant ID cannot be empty");
            }
            
            // Query restaurant
            Restaurant restaurant = restaurantMapper.selectById(id);
            if (restaurant == null) {
                return Result.validateFailed("Restaurant not found");
            }
            
            // Permission check: Only restaurant owner or admin can delete restaurant
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.validateFailed("User not found");
            }
            
            // Admin(role=0) can delete any restaurant, restaurant manager(role=1) can only delete their own restaurants
            if (user.getRole() != 0 && !userId.equals(restaurant.getOwnerId())) {
                return Result.forbidden("No permission to delete restaurants created by others");
            }
            
            // Delete restaurant
            restaurantMapper.deleteById(id);
            
            return Result.success(null, "Deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete restaurant", e);
            return Result.error(500, "Failed to delete restaurant: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateRestaurantStatus(Integer id, Integer status, Integer userId) {
        try {
            // Parameter validation
            if (id == null) {
                return Result.validateFailed("Restaurant ID cannot be empty");
            }
            
            if (status == null || status < 0 || status > 3) {
                return Result.validateFailed("Invalid restaurant status");
            }
            
            // Query restaurant
            Restaurant restaurant = restaurantMapper.selectById(id);
            if (restaurant == null) {
                return Result.validateFailed("Restaurant not found");
            }
            
            // Permission check: Only restaurant owner or admin can update restaurant status
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.validateFailed("User not found");
            }
            
            // Admin(role=0) can update any restaurant status, restaurant manager(role=1) can only update their own restaurants
            if (user.getRole() != 0 && !userId.equals(restaurant.getOwnerId())) {
                return Result.forbidden("No permission to update status of restaurants created by others");
            }
            
            // Update status
            restaurant.setStatus(status);
            restaurant.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            restaurantMapper.updateById(restaurant);
            
            // Build return data
            Map<String, Object> result = new HashMap<>();
            result.put("id", restaurant.getId());
            result.put("status", status);
            result.put("statusText", getStatusText(status));
            
            return Result.success(result, "Status updated successfully");
        } catch (Exception e) {
            log.error("Failed to update restaurant status", e);
            return Result.error(500, "Failed to update restaurant status: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Map<String, Object>> checkManagerRole(String token) {
        // Token validation
        if (token == null || token.isEmpty()) {
            return Result.unauthorized();
        }
        
        try {
            // Parse user role from token
            Claims allClaimsFromToken = jwtTokenUtil.getAllClaimsFromToken(token);
            Integer role = allClaimsFromToken.get("role", Integer.class);
            if (role == null || (role != 0 && role != 1)) {
                return Result.forbidden("No permission to access management interface, administrator or restaurant manager role required");
            }
            
            // Parse user ID
            Integer userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.unauthorized();
            }
            
            // Build return data
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("role", role);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to parse JWT token", e);
            return Result.unauthorized();
        }
    }
    
    @Override
    public Result<Map<String, Object>> getManagerRestaurantList(Integer page, Integer pageSize, String keyword, Integer status, Integer managerId) {
        try {
            // Parameter validation
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }
            if (managerId == null) {
                return Result.validateFailed("Restaurant manager ID cannot be empty");
            }
            
            // Calculate offset
            int offset = (page - 1) * pageSize;
            
            // Build query conditions
            LambdaQueryWrapper<Restaurant> queryWrapper = new LambdaQueryWrapper<>();
            
            // Only query restaurants owned by current manager
            queryWrapper.eq(Restaurant::getOwnerId, managerId);
            
            // Keyword search
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> 
                    wrapper.like(Restaurant::getName, keyword)
                        .or()
                        .like(Restaurant::getAddress, keyword)
                );
            }
            
            // Status filter
            if (status != null) {
                queryWrapper.eq(Restaurant::getStatus, status);
            }
            
            // Query total count
            long total = restaurantMapper.selectCount(queryWrapper);
            
            // Sort
            queryWrapper.orderByDesc(Restaurant::getId);
            
            // Pagination
            queryWrapper.last("LIMIT " + pageSize + " OFFSET " + offset);
            
            // Query list
            List<Restaurant> restaurantList = restaurantMapper.selectList(queryWrapper);
            
            // Convert to VO objects
            List<RestaurantVO> restaurantVOList = new ArrayList<>();
            
            // Get current manager user information (for setting ownerName)
            User manager = userMapper.selectById(managerId);
            String managerName = manager != null ? manager.getName() : "";
            
            for (Restaurant restaurant : restaurantList) {
                RestaurantVO restaurantVO = convertToVO(restaurant);
                restaurantVO.setOwnerName(managerName);
                restaurantVOList.add(restaurantVO);
            }
            
            // Build return result
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", restaurantVOList);
            
            return Result.success(result, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get manager's restaurant list", e);
            return Result.error(500, "Failed to get manager's restaurant list: " + e.getMessage());
        }
    }
    
    /**
     * Convert Restaurant entity to RestaurantVO
     * @param restaurant Restaurant entity
     * @return Restaurant VO
     */
    private RestaurantVO convertToVO(Restaurant restaurant) {
        RestaurantVO restaurantVO = new RestaurantVO();
        BeanUtils.copyProperties(restaurant, restaurantVO);
        
        // Set owner ID
        restaurantVO.setOwnerId(restaurant.getOwnerId());
        
        // Process photo list
        if (restaurant.getPhotos() != null && !restaurant.getPhotos().isEmpty()) {
            List<String> photoList = Arrays.asList(restaurant.getPhotos().split("\\|"));
            restaurantVO.setPhotos(photoList);
        }
        
        // Process coordinates
        if (restaurant.getLongitude() != null) {
            restaurantVO.setLongitude(restaurant.getLongitude());
        }
        
        if (restaurant.getLatitude() != null) {
            restaurantVO.setLatitude(restaurant.getLatitude());
        }

        // Set status text
        restaurantVO.setStatus(restaurant.getStatus().toString());
        
        return restaurantVO;
    }
    
    /**
     * Get restaurant status text
     * @param status Status value
     * @return Status text
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "Unknown";
        }
        
        switch (status) {
            case 0:
                return "Pending Review";
            case 1:
                return "Approved";
            case 2:
                return "Closed";
            case 3:
                return "Operating";
            default:
                return "Unknown";
        }
    }
}