package com.room.hd.dto;

import lombok.Data;

/**
 * Cancel Reservation DTO
 */
@Data
public class CancelReservationDTO {
    
    /**
     * Cancellation reason
     */
    private String cancelReason;
} 