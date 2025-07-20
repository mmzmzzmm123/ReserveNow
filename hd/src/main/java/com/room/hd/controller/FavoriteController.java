package com.room.hd.controller;

import com.room.hd.common.Result;
import com.room.hd.service.FavoriteService;
import com.room.hd.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Favorite Controller
 */
@RestController
@RequestMapping("/favorites")
@CrossOrigin
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * Add restaurant to favorites
     * @param params Request body containing restaurant ID
     * @param request HTTP request containing user ID
     * @return Favorite result
     */
    @PostMapping
    public Result<?> addFavorite(@RequestBody Map<String, Integer> params, HttpServletRequest request) {
        Integer restaurantId = params.get("restaurantId");
        if (restaurantId == null) {
            return Result.validateFailed("Restaurant ID cannot be empty");
        }
        
        // Get user ID from token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return Result.unauthorized();
        }
        
        try {
            Integer userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.unauthorized();
            }
            
            return favoriteService.addFavorite(restaurantId, userId);
        } catch (Exception e) {
            return Result.error(500, "Failed to add to favorites: " + e.getMessage());
        }
    }

    /**
     * Remove restaurant from favorites
     * @param restaurantId Restaurant ID
     * @param request HTTP request containing user ID
     * @return Remove favorite result
     */
    @DeleteMapping("/{restaurantId}")
    public Result<?> removeFavorite(@PathVariable Integer restaurantId, HttpServletRequest request) {
        if (restaurantId == null) {
            return Result.validateFailed("Restaurant ID cannot be empty");
        }
        
        // Get user ID from token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return Result.unauthorized();
        }
        
        try {
            Integer userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.unauthorized();
            }
            
            return favoriteService.removeFavorite(restaurantId, userId);
        } catch (Exception e) {
            return Result.error(500, "Failed to remove from favorites: " + e.getMessage());
        }
    }
    
    /**
     * Get favorite list
     * @param page Current page number
     * @param pageSize Items per page
     * @param request HTTP request containing user ID
     * @return Favorite list
     */
    @GetMapping
    public Result<?> getFavorites(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
            
        // Get user ID from token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return Result.unauthorized();
        }
        
        try {
            Integer userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.unauthorized();
            }
            
            return favoriteService.getFavoriteList(page, pageSize, userId);
        } catch (Exception e) {
            return Result.error(500, "Failed to get favorite list: " + e.getMessage());
        }
    }
} 