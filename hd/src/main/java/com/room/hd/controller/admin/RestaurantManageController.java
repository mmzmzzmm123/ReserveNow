package com.room.hd.controller.admin;

import com.room.hd.common.Result;
import com.room.hd.service.RestaurantManageService;
import com.room.hd.vo.RestaurantVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Restaurant Management Controller
 */
@RestController
@RequestMapping("/admin/restaurants")
@CrossOrigin
public class RestaurantManageController {

    @Autowired
    private RestaurantManageService restaurantManageService;
    
    /**
     * Get restaurant list
     * @param page Page number
     * @param pageSize Items per page
     * @param keyword Search keyword (restaurant name, address)
     * @param status Status filter (optional)
     * @param request HTTP request containing user JWT token
     * @return Restaurant list
     */
    @GetMapping
    public Result<Map<String, Object>> getRestaurantList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkManagerRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access management interface");
        }
        
        // Get restaurant list
        return restaurantManageService.getRestaurantList(page, pageSize, keyword, status);
    }
    
    /**
     * Get restaurant details
     * @param id Restaurant ID
     * @param request HTTP request containing user JWT token
     * @return Restaurant details
     */
    @GetMapping("/{id}")
    public Result<RestaurantVO> getRestaurantDetail(
            @PathVariable Integer id,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkManagerRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access management interface");
        }
        
        // Get restaurant details
        return restaurantManageService.getRestaurantDetail(id);
    }
    
    /**
     * Add restaurant
     * @param restaurantVO Restaurant information
     * @param request HTTP request containing user JWT token
     * @return Add result
     */
    @PostMapping
    public Result<RestaurantVO> addRestaurant(
            @RequestBody RestaurantVO restaurantVO,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkManagerRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access management interface");
        }
        
        // Add restaurant
        Integer userId = (Integer) roleCheck.getData().get("userId");
        return restaurantManageService.addRestaurant(restaurantVO, userId);
    }
    
    /**
     * Update restaurant information
     * @param id Restaurant ID
     * @param restaurantVO Restaurant information
     * @param request HTTP request containing user JWT token
     * @return Update result
     */
    @PutMapping("/{id}")
    public Result<RestaurantVO> updateRestaurant(
            @PathVariable Integer id,
            @RequestBody RestaurantVO restaurantVO,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkManagerRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access management interface");
        }
        
        // Set restaurant ID
        restaurantVO.setId(id);
        
        // Update restaurant information
        Integer userId = (Integer) roleCheck.getData().get("userId");
        return restaurantManageService.updateRestaurant(id, restaurantVO, userId);
    }
    
    /**
     * Delete restaurant
     * @param id Restaurant ID
     * @param request HTTP request containing user JWT token
     * @return Delete result
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteRestaurant(
            @PathVariable Integer id,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkManagerRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access management interface");
        }
        
        // Delete restaurant
        Integer userId = (Integer) roleCheck.getData().get("userId");
        return restaurantManageService.deleteRestaurant(id, userId);
    }
    
    /**
     * Update restaurant status
     * @param id Restaurant ID
     * @param status Status value
     * @param request HTTP request containing user JWT token
     * @return Update result
     */
    @PutMapping("/{id}/status")
    public Result<?> updateRestaurantStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkManagerRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access management interface");
        }
        
        // Update restaurant status
        Integer userId = (Integer) roleCheck.getData().get("userId");
        return restaurantManageService.updateRestaurantStatus(id, status, userId);
    }
    
    /**
     * Get restaurant manager's own restaurant list
     * @param page Page number
     * @param pageSize Items per page
     * @param keyword Search keyword (restaurant name, address)
     * @param status Status filter (optional)
     * @param request HTTP request containing user JWT token
     * @return Restaurant list
     */
    @GetMapping("/my-restaurants")
    public Result<Map<String, Object>> getMyRestaurantList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            HttpServletRequest request) {
        
        // Permission verification
        Result<Map<String, Object>> roleCheck = checkManagerRole(request);
        if (!roleCheck.isSuccess()) {
            return Result.forbidden("No permission to access management interface");
        }
        
        // Get current user ID
        Integer userId = (Integer) roleCheck.getData().get("userId");
        
        // Get restaurant manager's restaurant list
        return restaurantManageService.getManagerRestaurantList(page, pageSize, keyword, status, userId);
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