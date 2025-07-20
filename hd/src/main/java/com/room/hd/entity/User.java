package com.room.hd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * User Entity Class
 */
@Data
@TableName("users")
public class User {
    
    /**
     * User ID, Primary Key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * User Name
     */
    @TableField("name")
    private String name;
    
    /**
     * User Email, Unique
     */
    @TableField("email")
    private String email;
    
    /**
     * User Password Hash
     */
    @TableField("password")
    private String password;
    
    /**
     * User Role: 0: Administrator, 1: Restaurant Manager, 2: User, 3: Staff
     */
    @TableField("role")
    private Integer role;
    
    /**
     * User Avatar URL
     */
    @TableField("avatar")
    private String avatar;
    
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
     * User Status: 0: Banned, 1: Enabled
     */
    @TableField("status")
    private Integer status;
} 