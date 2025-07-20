package com.room.hd.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite Primary Key for Favorites Table
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteKey implements Serializable {
    
    /**
     * Restaurant ID
     */
    private Integer restaurantId;
    
    /**
     * User ID
     */
    private Integer userId;
} 