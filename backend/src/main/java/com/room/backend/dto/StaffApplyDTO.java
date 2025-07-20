package com.room.backend.dto;

import lombok.Data;

/**
 * Staff Application DTO
 */
@Data
public class StaffApplyDTO {
    
    /**
     * User ID (Applicant)
     */
    private Integer userId;
    
    /**
     * Manager ID
     */
    private Integer managerId;
} 