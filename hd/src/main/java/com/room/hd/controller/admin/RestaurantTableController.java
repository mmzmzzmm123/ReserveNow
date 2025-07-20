package com.room.hd.controller.admin;

import com.room.hd.common.Result;
import com.room.hd.entity.RestaurantTable;
import com.room.hd.service.RestaurantManageService;
import com.room.hd.service.RestaurantTableService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Restaurant Table Management Controller
 */
@RestController
@RequestMapping("/admin/tables")
@CrossOrigin
public class RestaurantTableController {

    @Autowired
    private RestaurantTableService tableService;
    
    @Autowired
    private RestaurantManageService restaurantManageService;
    
    /**
     * Get all tables list (for administrator)
     * @param page Page number
     * @param pageSize Items per page
     * @param restaurantId Restaurant ID (optional)
     * @param request HTTP request containing user JWT token
     * @return Table list
     */
    @GetMapping
    public Result<Map<String, Object>> getAllTables(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "restaurantId", required = false) Integer restaurantId,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkManagerRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access management interface");
        }
        
        // Only administrator can view all tables
        Integer role = (Integer) roleCheck.getData().get("role");
        if (role != 0) {
            return Result.forbidden("Only administrator can view all tables");
        }
        
        // Get all tables list
        return tableService.getAllTables(page, pageSize, restaurantId);
    }
    
    /**
     * Get tables list for specific restaurant (for restaurant manager)
     * @param page Page number
     * @param pageSize Items per page
     * @param restaurantId Restaurant ID
     * @param request HTTP request containing user JWT token
     * @return Table list
     */
    @GetMapping("/restaurant/{restaurantId}")
    public Result<Map<String, Object>> getRestaurantTables(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @PathVariable Integer restaurantId,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkManagerRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access management interface");
        }
        
        // Get current user ID
        Integer userId = (Integer) roleCheck.getData().get("userId");
        
        // Get tables list for specific restaurant
        return tableService.getRestaurantTables(page, pageSize, restaurantId, userId);
    }
    
    /**
     * Get table details
     * @param id Table ID
     * @param request HTTP request containing user JWT token
     * @return Table details
     */
    @GetMapping("/{id}")
    public Result<RestaurantTable> getTableDetail(
            @PathVariable Integer id,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkManagerRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access management interface");
        }
        
        // Get table details
        return tableService.getTableDetail(id);
    }
    
    /**
     * Add table
     * @param table Table information
     * @param request HTTP request containing user JWT token
     * @return Add result
     */
    @PostMapping
    public Result<RestaurantTable> addTable(
            @RequestBody RestaurantTable table,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkManagerRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access management interface");
        }
        
        // Get current user ID
        Integer userId = (Integer) roleCheck.getData().get("userId");
        
        // Add table
        return tableService.addTable(table, userId);
    }
    
    /**
     * Update table information
     * @param id Table ID
     * @param table Table information
     * @param request HTTP request containing user JWT token
     * @return Update result
     */
    @PutMapping("/{id}")
    public Result<RestaurantTable> updateTable(
            @PathVariable Integer id,
            @RequestBody RestaurantTable table,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkManagerRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access management interface");
        }
        
        // Get current user ID
        Integer userId = (Integer) roleCheck.getData().get("userId");
        
        // Update table information
        return tableService.updateTable(id, table, userId);
    }
    
    /**
     * Delete table
     * @param id Table ID
     * @param request HTTP request containing user JWT token
     * @return Delete result
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteTable(
            @PathVariable Integer id,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkManagerRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access management interface");
        }
        
        // Get current user ID
        Integer userId = (Integer) roleCheck.getData().get("userId");
        
        // Delete table
        return tableService.deleteTable(id, userId);
    }
    
    /**
     * Get all tables under current restaurant manager
     * @param page Page number
     * @param pageSize Items per page
     * @param request HTTP request containing user JWT token
     * @return Table list
     */
    @GetMapping("/my-tables")
    public Result<Map<String, Object>> getManagerAllTables(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkManagerRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access management interface");
        }
        
        // Get current user ID and role
        Integer userId = (Integer) roleCheck.getData().get("userId");
        Integer role = (Integer) roleCheck.getData().get("role");
        
        // Only restaurant manager can use this interface
        if (role != 1) {
            return Result.forbidden("Only restaurant manager can use this interface");
        }
        
        // Get all tables under current restaurant manager
        return tableService.getManagerAllTables(page, pageSize, userId);
    }
    
    /**
     * Check if user has administrator or restaurant manager role
     * @param request HTTP request containing user JWT token
     * @return Check result
     */
    private Result<Map<String, Object>> checkManagerRole(HttpServletRequest request) {
        // Get token from request header
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return Result.unauthorized();
        }
        
        // Check role permissions
        return restaurantManageService.checkManagerRole(token);
    }
}