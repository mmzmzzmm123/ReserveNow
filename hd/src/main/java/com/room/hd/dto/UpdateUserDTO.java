package com.room.hd.dto;

import lombok.Data;

/**
 * Update User Information DTO
 */
@Data
public class UpdateUserDTO {
    
    /**
     * Username
     */
    private String name;
    
    /**
     * Avatar URL
     */
    private String avatar;
    
    /**
     * Old password (optional, only required when changing password)
     */
    private String oldPassword;
    
    /**
     * New password (optional, only required when changing password)
     */
    private String newPassword;
    
    /**
     * Confirm new password (optional, only required when changing password)
     */
    private String confirmPassword;
} 