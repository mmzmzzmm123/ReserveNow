package com.room.hd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Favorite Entity Class
 */
@Data
@TableName("favorites")
public class Favorite implements Serializable {
    
    /**
     * Favorite ID, Primary Key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * Restaurant ID
     */
    @TableField("restaurant_id")
    private Integer restaurantId;
    
    /**
     * User ID
     */
    @TableField("user_id")
    private Integer userId;
    
    /**
     * Creation Time
     */
    @TableField("created_at")
    private OffsetDateTime createdAt;
} 