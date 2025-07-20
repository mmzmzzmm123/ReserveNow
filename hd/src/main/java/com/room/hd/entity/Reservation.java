package com.room.hd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Reservation Entity Class
 */
@Data
@TableName("reservations")
public class Reservation {
    
    /**
     * Reservation ID, Primary Key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
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
     * Table ID, Foreign Key referencing tables table
     */
    @TableField("table_id")
    private Integer tableId;

    /**
     * Review ID, Foreign Key referencing reviews table
     */
    @TableField("review_id")
    private String reviewId;
    
    /**
     * Reservation Time
     */
    @TableField("reservation_time")
    private OffsetDateTime reservationTime;
    
    /**
     * Reservation Duration (hours)
     */
    @TableField("reservation_date")
    private Integer reservationDate;
    
    /**
     * Number of People
     */
    @TableField("person_count")
    private Integer personCount;
    
    /**
     * Remarks
     */
    @TableField("remarks")
    private String remarks;
    
    /**
     * Reservation Status: 0: Pending Confirmation, 1: Confirmed, 2: Cancelled, 3: Completed, 4: No-show
     */
    @TableField("status")
    private Integer status;
    
    /**
     * Cancellation Reason
     */
    @TableField("cancel_reason")
    private String cancelReason;
    
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