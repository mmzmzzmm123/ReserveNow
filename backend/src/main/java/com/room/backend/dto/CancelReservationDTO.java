package com.room.backend.dto;

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