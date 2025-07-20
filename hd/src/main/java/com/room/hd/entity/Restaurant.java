package com.room.hd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Restaurant Entity Class
 */
@Data
@TableName("restaurants")
public class Restaurant {
    
    /**
     * Restaurant ID, Primary Key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * Owner ID, Foreign Key referencing users table
     */
    @TableField("owner_id")
    private Integer ownerId;
    
    /**
     * Restaurant Name
     */
    @TableField("name")
    private String name;
    
    /**
     * Restaurant Description
     */
    @TableField("description")
    private String description;
    
    /**
     * Restaurant Address
     */
    @TableField("address")
    private String address;

    /**
     * Longitude
     */
    @TableField("longitude")
    private Double longitude;

    /**
     * Latitude
     */
    @TableField("latitude")
    private Double latitude;

    /**
     * Restaurant Phone
     */
    @TableField("phone")
    private String phone;
    
    /**
     * Business License
     */
    @TableField("business_license")
    private String businessLicense;
    
    /**
     * Restaurant Photos, URLs separated by |
     */
    @TableField("photos")
    private String photos;
    
    /**
     * Restaurant Status: 0: Pending Review, 1: Approved, 2: Closed, 3: Operating
     */
    @TableField("status")
    private Integer status;
    
    /**
     * Business Hours
     */
    @TableField("business_hours")
    private String businessHours;
    
    /**
     * Cuisine Type, e.g., Chinese, Western, Japanese, Korean, etc.
     */
    @TableField("cuisine")
    private String cuisine;
    
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
} 