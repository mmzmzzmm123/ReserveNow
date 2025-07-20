package com.room.hd.vo;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Favorite Restaurant VO Class
 */
@Data
public class FavoriteRestaurantVO {
    
    /**
     * Restaurant ID
     */
    private Integer restaurantId;
    
    /**
     * Restaurant Name
     */
    private String restaurantName;
    
    /**
     * Restaurant Description
     */
    private String description;
    
    /**
     * Restaurant Address
     */
    private String address;
    
    /**
     * List of Restaurant Photos
     */
    private List<String> photos;
    
    /**
     * Cuisine Type (e.g., Chinese, Western, Japanese, Korean)
     */
    private String cuisine;
    
    /**
     * Restaurant Average Rating
     */
    private Double rating;
    
    /**
     * Favorite Time
     */
    private OffsetDateTime createdAt;
} 