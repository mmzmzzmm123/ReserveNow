package com.room.hd.vo;

import com.room.hd.entity.RestaurantTable;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Restaurant VO Class
 */
@Data
public class RestaurantVO {
    
    /**
     * Restaurant ID
     */
    private Integer id;
    
    /**
     * Restaurant Name
     */
    private String name;
    
    /**
     * Restaurant Description
     */
    private String description;
    
    /**
     * Restaurant Address
     */
    private String address;

    /**
     * Restaurant Longitude
     */
    private Double longitude;

    /**
     * Restaurant Latitude
     */
    private Double latitude;

    /**
     * Restaurant Phone
     */
    private String phone;
    
    /**
     * Business License
     */
    private String businessLicense;
    
    /**
     * List of Restaurant Photos
     */
    private List<String> photos;
    
    /**
     * Restaurant Status
     */
    private String status;
    
    /**
     * Business Hours
     */
    private String businessHours;
    
    /**
     * Cuisine Type (e.g., Chinese, Western, Japanese, Korean)
     */
    private String cuisine;
    
    /**
     * Creation Time
     */
    private OffsetDateTime createdAt;
    
    /**
     * Update Time
     */
    private OffsetDateTime updatedAt;
    
    /**
     * Owner ID
     */
    private Integer ownerId;
    
    /**
     * Owner Name
     */
    private String ownerName;
    
    /**
     * Is Favorite
     */
    private Boolean isFavorite;
    
    /**
     * Restaurant Average Rating
     */
    private Double rating;
    
    /**
     * Review Count
     */
    private Integer reviewCount;
    
    /**
     * List of Restaurant Tables
     */
    private List<RestaurantTable> tables;
} 