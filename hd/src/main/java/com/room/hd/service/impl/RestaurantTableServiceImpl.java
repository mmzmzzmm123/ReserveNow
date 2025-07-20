package com.room.hd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.room.hd.common.Result;
import com.room.hd.entity.Restaurant;
import com.room.hd.entity.RestaurantTable;
import com.room.hd.entity.User;
import com.room.hd.mapper.RestaurantMapper;
import com.room.hd.mapper.RestaurantTableMapper;
import com.room.hd.mapper.UserMapper;
import com.room.hd.service.RestaurantTableService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Restaurant Table Management Service Implementation
 */
@Service
@Slf4j
public class RestaurantTableServiceImpl implements RestaurantTableService {

    @Resource
    private RestaurantTableMapper tableMapper;
    
    @Resource
    private RestaurantMapper restaurantMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Override
    public Result<Map<String, Object>> getAllTables(Integer page, Integer pageSize, Integer restaurantId) {
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
            LambdaQueryWrapper<RestaurantTable> queryWrapper = new LambdaQueryWrapper<>();
            
            // If restaurant ID is provided, only query tables for that restaurant
            if (restaurantId != null) {
                System.out.println("restaurantId: " + restaurantId);
                queryWrapper.eq(RestaurantTable::getRestaurantId, restaurantId);
            }
            
            // Query total count
            long total = tableMapper.selectCount(queryWrapper);
            
            // Sort
            queryWrapper.orderByDesc(RestaurantTable::getId);
            
            // Pagination
            queryWrapper.last("LIMIT " + pageSize + " OFFSET " + offset);
            
            // Query list
            List<RestaurantTable> tableList = tableMapper.selectList(queryWrapper);
            
            // Get all involved restaurant IDs
            List<Integer> restaurantIds = tableList.stream()
                    .map(RestaurantTable::getRestaurantId)
                    .distinct()
                    .toList();

            if (restaurantIds.isEmpty()) {
                // If no related restaurant IDs, return empty list directly
                Map<String, Object> result = new HashMap<>();
                result.put("total", 0);
                result.put("list", new ArrayList<>());
                return Result.success(result, "Retrieved successfully");
            }
            // Query restaurant information
            LambdaQueryWrapper<Restaurant> restaurantQuery = new LambdaQueryWrapper<>();
            restaurantQuery.in(Restaurant::getId, restaurantIds);
            List<Restaurant> restaurants = restaurantMapper.selectList(restaurantQuery);
            
            // Create a Map to store restaurant ID and name mapping
            Map<Integer, String> restaurantNameMap = new HashMap<>();
            for (Restaurant restaurant : restaurants) {
                restaurantNameMap.put(restaurant.getId(), restaurant.getName());
            }
            
            // Build result list, add restaurant names
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (RestaurantTable table : tableList) {
                Map<String, Object> tableMap = new HashMap<>();
                tableMap.put("id", table.getId());
                tableMap.put("restaurantId", table.getRestaurantId());
                tableMap.put("restaurantName", restaurantNameMap.get(table.getRestaurantId()));
                tableMap.put("type", table.getType());
                tableMap.put("capacity", table.getCapacity());
                tableMap.put("createdAt", table.getCreatedAt());
                tableMap.put("updatedAt", table.getUpdatedAt());
                resultList.add(tableMap);
            }
            
            // Build return result
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", resultList);
            
            return Result.success(result, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get table list", e);
            return Result.error(500, "Failed to get table list: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Map<String, Object>> getRestaurantTables(Integer page, Integer pageSize, Integer restaurantId, Integer managerId) {
        try {
            // Parameter validation
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }
            if (restaurantId == null) {
                return Result.validateFailed("Restaurant ID cannot be empty");
            }
            
            // Check permission
            Result<Boolean> permissionResult = checkRestaurantPermission(managerId, restaurantId);
            if (!permissionResult.isSuccess() || !permissionResult.getData()) {
                return Result.forbidden("No permission to view tables for this restaurant");
            }
            
            // Calculate offset
            int offset = (page - 1) * pageSize;
            
            // Build query conditions
            LambdaQueryWrapper<RestaurantTable> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RestaurantTable::getRestaurantId, restaurantId);
            
            // Query total count
            long total = tableMapper.selectCount(queryWrapper);
            
            // Sort
            queryWrapper.orderByDesc(RestaurantTable::getId);
            
            // Pagination
            queryWrapper.last("LIMIT " + pageSize + " OFFSET " + offset);
            
            // Query list
            List<RestaurantTable> tableList = tableMapper.selectList(queryWrapper);
            
            // Query restaurant information
            Restaurant restaurant = restaurantMapper.selectById(restaurantId);
            String restaurantName = restaurant != null ? restaurant.getName() : "";
            
            // Build result list, add restaurant name
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (RestaurantTable table : tableList) {
                Map<String, Object> tableMap = new HashMap<>();
                tableMap.put("id", table.getId());
                tableMap.put("restaurantId", table.getRestaurantId());
                tableMap.put("restaurantName", restaurantName);
                tableMap.put("type", table.getType());
                tableMap.put("capacity", table.getCapacity());
                tableMap.put("createdAt", table.getCreatedAt());
                tableMap.put("updatedAt", table.getUpdatedAt());
                resultList.add(tableMap);
            }
            
            // Build return result
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", resultList);
            
            return Result.success(result, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get restaurant table list", e);
            return Result.error(500, "Failed to get restaurant table list: " + e.getMessage());
        }
    }
    
    @Override
    public Result<RestaurantTable> getTableDetail(Integer id) {
        try {
            // Parameter validation
            if (id == null) {
                return Result.validateFailed("Table ID cannot be empty");
            }
            
            // Query table
            RestaurantTable table = tableMapper.selectById(id);
            if (table == null) {
                return Result.validateFailed("Table not found");
            }
            
            return Result.success(table, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get table details", e);
            return Result.error(500, "Failed to get table details: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RestaurantTable> addTable(RestaurantTable table, Integer userId) {
        try {
            // Parameter validation
            if (table == null || table.getRestaurantId() == null || table.getType() == null || table.getCapacity() == null) {
                return Result.validateFailed("Table information is incomplete");
            }
            
            // Check if restaurant exists
            Restaurant restaurant = restaurantMapper.selectById(table.getRestaurantId());
            if (restaurant == null) {
                return Result.validateFailed("Restaurant not found");
            }
            
            // Check permission
            Result<Boolean> permissionResult = checkRestaurantPermission(userId, table.getRestaurantId());
            if (!permissionResult.isSuccess() || !permissionResult.getData()) {
                return Result.forbidden("No permission to add tables for this restaurant");
            }
            
            // Set creation and update time
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            table.setCreatedAt(now);
            table.setUpdatedAt(now);
            
            // Add table
            tableMapper.insert(table);
            
            return Result.success(table, "Added successfully");
        } catch (Exception e) {
            log.error("Failed to add table", e);
            return Result.error(500, "Failed to add table: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RestaurantTable> updateTable(Integer id, RestaurantTable table, Integer userId) {
        try {
            // Parameter validation
            if (id == null) {
                return Result.validateFailed("Table ID cannot be empty");
            }
            
            // Query table
            RestaurantTable existingTable = tableMapper.selectById(id);
            if (existingTable == null) {
                return Result.validateFailed("Table not found");
            }
            
            // Check permission
            Result<Boolean> permissionResult = checkRestaurantPermission(userId, existingTable.getRestaurantId());
            if (!permissionResult.isSuccess() || !permissionResult.getData()) {
                return Result.forbidden("No permission to update tables for this restaurant");
            }
            
            // Update table information
            if (table.getType() != null) {
                existingTable.setType(table.getType());
            }
            if (table.getCapacity() != null) {
                existingTable.setCapacity(table.getCapacity());
            }
            
            // Update timestamp
            existingTable.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            
            // Save changes
            tableMapper.updateById(existingTable);
            
            return Result.success(existingTable, "Updated successfully");
        } catch (Exception e) {
            log.error("Failed to update table", e);
            return Result.error(500, "Failed to update table: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteTable(Integer id, Integer userId) {
        try {
            // Parameter validation
            if (id == null) {
                return Result.validateFailed("Table ID cannot be empty");
            }
            
            // Query table
            RestaurantTable table = tableMapper.selectById(id);
            if (table == null) {
                return Result.validateFailed("Table not found");
            }
            
            // Check permission
            Result<Boolean> permissionResult = checkRestaurantPermission(userId, table.getRestaurantId());
            if (!permissionResult.isSuccess() || !permissionResult.getData()) {
                return Result.forbidden("No permission to delete tables for this restaurant");
            }
            
            // Delete table
            tableMapper.deleteById(id);
            
            return Result.success(null, "Deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete table", e);
            return Result.error(500, "Failed to delete table: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Boolean> checkRestaurantPermission(Integer userId, Integer restaurantId) {
        try {
            // Parameter validation
            if (userId == null) {
                return Result.validateFailed("User ID cannot be empty");
            }
            
            if (restaurantId == null) {
                return Result.validateFailed("Restaurant ID cannot be empty");
            }
            
            // Query user
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.validateFailed("User not found");
            }
            
            // Query restaurant
            Restaurant restaurant = restaurantMapper.selectById(restaurantId);
            if (restaurant == null) {
                return Result.validateFailed("Restaurant not found");
            }
            
            // admin has all permissions
            if (user.getRole() == 0) {
                return Result.success(true);
            }
            
            // restaurant manager can only manage their own restaurant
            System.out.println(restaurant.getOwnerId());
            System.out.println(userId);
            if (user.getRole() == 1 && restaurant.getOwnerId().equals(userId)) {
                return Result.success(true);
            }
            System.out.println("checkRestaurantPermission"+restaurant.getOwnerId().equals(userId));
            
            return Result.success(false);
        } catch (Exception e) {
            log.error("Failed to check restaurant permission", e);
            return Result.error(500, "Failed to check restaurant permission: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Map<String, Object>> getManagerAllTables(Integer page, Integer pageSize, Integer managerId) {
        try {
            // Parameter validation
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }
            if (managerId == null) {
                return Result.validateFailed("Manager ID cannot be empty");
            }
            
            // Query user
            User user = userMapper.selectById(managerId);
            if (user == null) {
                return Result.validateFailed("User not found");
            }
            
            // check user role
            if (user.getRole() != 1) {
                return Result.forbidden("Only restaurant managers can use this interface");
            }
            
            // first query all restaurant ids that the manager owns
            LambdaQueryWrapper<Restaurant> restaurantQuery = new LambdaQueryWrapper<>();
            restaurantQuery.eq(Restaurant::getOwnerId, managerId);
            List<Restaurant> restaurants = restaurantMapper.selectList(restaurantQuery);
            
            if (restaurants.isEmpty()) {
                // when there are no restaurants, return empty result
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("total", 0);
                emptyResult.put("list", List.of());
                return Result.success(emptyResult, "Retrieved successfully");
            }
            
            // get all restaurant ids
            List<Integer> restaurantIds = restaurants.stream()
                    .map(Restaurant::getId)
                    .toList();
            
            // calculate offset
            int offset = (page - 1) * pageSize;
            
            // Build query conditions
            LambdaQueryWrapper<RestaurantTable> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(RestaurantTable::getRestaurantId, restaurantIds);
            
            // Query total count
            long total = tableMapper.selectCount(queryWrapper);
            
            // Sort
            queryWrapper.orderByDesc(RestaurantTable::getId);
            
            // Pagination
            queryWrapper.last("LIMIT " + pageSize + " OFFSET " + offset);
            
            // Query list
            List<RestaurantTable> tableList = tableMapper.selectList(queryWrapper);
            
            // create a map to store restaurant id and name mapping
            Map<Integer, String> restaurantNameMap = new HashMap<>();
            for (Restaurant restaurant : restaurants) {
                restaurantNameMap.put(restaurant.getId(), restaurant.getName());
            }
            
            // Build result list, add restaurant names
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (RestaurantTable table : tableList) {
                Map<String, Object> tableMap = new HashMap<>();
                tableMap.put("id", table.getId());
                tableMap.put("restaurantId", table.getRestaurantId());
                tableMap.put("restaurantName", restaurantNameMap.get(table.getRestaurantId()));
                tableMap.put("type", table.getType());
                tableMap.put("capacity", table.getCapacity());
                tableMap.put("createdAt", table.getCreatedAt());
                tableMap.put("updatedAt", table.getUpdatedAt());
                resultList.add(tableMap);
            }
            
            // Build return result
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", resultList);
            
            return Result.success(result, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get manager's table list", e);
            return Result.error(500, "Failed to get manager's table list: " + e.getMessage());
        }
    }
}