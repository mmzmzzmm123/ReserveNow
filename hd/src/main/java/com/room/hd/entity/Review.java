package com.room.hd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Review Entity Class
 */
@Data
@TableName("reviews")
public class Review {
    
    /**
     * Review ID, Primary Key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private String id;
    
    /**
     * Restaurant ID, Foreign Key referencing restaurants table
     */
    @TableField("restaurant_id")
    private Integer restaurantId;
    
    /**
     * User ID, Foreign Key referencing users table
     */
    @TableField("user_id")
    private Integer userId;
    
    /**
     * Review Content
     */
    @TableField("content")
    private String content;
    
    /**
     * Review Photos, URLs separated by |
     */
    @TableField("photos")
    private String photos;
    
    /**
     * Review Videos, URLs separated by |
     */
    @TableField("videos")
    private String videos;
    
    /**
     * Rating, 1-5 points
     */
    @TableField("rating")
    private Integer rating;
    
    /**
     * Creation Time
     */
    @TableField("created_at")
    private OffsetDateTime createdAt;
    
    /**
     * Update Time
     */
    @TableField("updated_at")
    private OffsetDateTime updatedAt;
    
    /**
     * Reservation ID, used to link back to the reservation
     */
    @TableField(exist = false)
    private Integer reservationId;
    
    // Related fields, not corresponding to database columns
    @TableField(exist = false)
    private String restaurantName;
    
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String userAvatar;
} 