package com.room.hd.vo;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;

import java.time.OffsetDateTime;

/**
 * Reservation Information VO
 */
@Data
public class ReservationVO {
    
    /**
     * Reservation ID
     */
    private Integer id;
    
    /**
     * Restaurant ID
     */
    private Integer restaurantId;
    
    /**
     * Restaurant Name
     */
    private String restaurantName;
    
    /**
     * Table ID
     */
    private Integer tableId;
    
    /**
     * Table Type
     */
    private String tableType;
    
    /**
     * Reservation Time
     */
    private OffsetDateTime reservationTime;
    
    /**
     * Reservation Duration (minutes)
     */
    private Integer reservationDate;
    
    /**
     * Number of People
     */
    private Integer personCount;
    
    /**
     * Remarks
     */
    private String remarks;
    
    /**
     * Reservation Status
     */
    private String status;
    
    /**
     * Reservation Status Numeric Value
     */
    private Integer statusValue;
    
    /**
     * Reservation Status Text
     */
    private String statusText;
    
    /**
     * Cancellation Reason
     */
    private String cancelReason;
    
    /**
     * Creation Time
     */
    private OffsetDateTime createdAt;
    
    /**
     * Update Time
     */
    private OffsetDateTime updatedAt;
    
    /**
     * 评价状态，true: 已评价，false: 未评价
     */
    @TableField(exist = false)
    private Boolean reviewed;
} 