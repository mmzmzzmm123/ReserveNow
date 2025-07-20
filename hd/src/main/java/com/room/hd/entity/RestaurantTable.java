package com.room.hd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Restaurant Table Entity Class
 */
@Data
@TableName("tables")
public class RestaurantTable {
    
    /**
     * Table ID, Primary Key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * Restaurant ID, Foreign Key referencing restaurants table
     */
    @TableField("restaurant_id")
    private Integer restaurantId;
    
    /**
     * Table Type
     */
    @TableField("type")
    private String type;
    
    /**
     * Seating Capacity
     */
    @TableField("capacity")
    private Integer capacity;
    
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