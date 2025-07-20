package com.room.hd.controller;

import com.room.hd.common.Result;
import com.room.hd.entity.User;
import com.room.hd.service.RestaurantService;
import com.room.hd.service.UserService;
import com.room.hd.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Restaurant Controller
 */
@RestController
@RequestMapping("/restaurants")
@CrossOrigin
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * Get restaurant list
     * @param request HTTP request containing user ID
     * @param page Current page number
     * @param pageSize Number of items per page
     * @param status Restaurant status
     * @param keyword Search keyword (restaurant name, address, description)
     * @return Restaurant list
     */
    @GetMapping
    public Result<Map<String, Object>> getRestaurantList(
            HttpServletRequest request,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        // Get token from request header
        String token = request.getHeader("Authorization");
        Integer userId = null;

        // If token exists, try to parse user ID
        if (token != null && !token.isEmpty()) {
            try {
                // Use JwtTokenUtil to parse token and get userId
                userId = jwtTokenUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                System.out.println("Failed to parse token: " + e);
            }
        }

        // If user is logged in, return restaurant list with favorite status
        if (userId != null) {
            return restaurantService.getRestaurantListWithFavorite(page, pageSize, status, keyword, userId);
        } else {
            // Return regular restaurant list for non-logged in users
            return restaurantService.getRestaurantList(page, pageSize, status, keyword);
        }
    }

    /**
     * Get restaurant details
     * @param id Restaurant ID
     * @param request HTTP request containing JWT token
     * @return Restaurant details
     */
    @GetMapping("/{id}")
    public Result<?> getRestaurantDetail(@PathVariable Integer id, HttpServletRequest request) {
        if (id == null) {
            return Result.validateFailed("Restaurant ID cannot be empty");
        }

        // Get JWT token from request header
        String token = request.getHeader("Authorization");

        // If token exists, try to get user information from token
        if (token != null && !token.isEmpty()) {
            try {
                // Directly use getUserIdFromToken method to get userId from token
                Integer userId = jwtTokenUtil.getUserIdFromToken(token);

                // If userId exists, return restaurant details with favorite status
                if (userId != null) {
                    return restaurantService.getRestaurantDetailWithFavorite(id, userId);
                }
            } catch (Exception e) {
                // Token parsing failed, ignore exception and return restaurant details without favorite status
            }
        }

        // Return restaurant details without favorite status for non-logged in users or invalid tokens
        return restaurantService.getRestaurantDetail(id);
    }
} 