package com.room.hd.dto;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Reservation Information DTO
 */
@Data
public class ReservationDTO {
    
    /**
     * Restaurant ID
     */
    private Integer restaurantId;
    
    /**
     * Table ID
     */
    private Integer tableId;
    
    /**
     * Reservation time
     */
    private OffsetDateTime reservationTime;
    
    /**
     * Reservation duration (minutes)
     */
    private Integer reservationDate;
    
    /**
     * Number of people
     */
    private Integer personCount;
    
    /**
     * Remarks
     */
    private String remarks;
} 