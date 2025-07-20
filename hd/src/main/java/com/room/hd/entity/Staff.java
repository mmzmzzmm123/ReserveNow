package com.room.hd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Staff Entity Class
 */
@Data
@TableName("staff")
public class Staff implements Serializable {
    
    /**
     * Staff ID, Primary Key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * Manager ID
     */
    @TableField("manager_id")
    private Integer managerId;
    
    /**
     * User ID
     */
    @TableField("user_id")
    private Integer userId;
    
    /**
     * Status (0: Pending Review, 1: Approved)
     */
    @TableField("status")
    private Integer status;
    
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
    
    // Non-database fields
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String userEmail;
} 