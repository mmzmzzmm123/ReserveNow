package com.room.hd.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * User View Object
 */
@Data
public class UserVO {
    
    /**
     * User ID
     */
    private Integer userId;
    
    /**
     * Username
     */
    private String name;
    
    /**
     * Email
     */
    private String email;
    
    /**
     * Password
     */
    private String password;
    
    /**
     * Confirm Password
     */
    private String confirmPassword;
    
    /**
     * Old Password
     */
    private String oldPassword;
    
    /**
     * New Password
     */
    private String newPassword;
    
    /**
     * Role Name
     */
    private Integer role;
    
    /**
     * Role Value (0: Administrator, 1: Restaurant Manager, 2: User, 3: Staff)
     */
    private Integer roleValue;
    
    /**
     * Status Name
     */
    private Integer status;
    
    /**
     * Status Value (0: Disabled, 1: Enabled)
     */
    private Integer statusValue;
    
    /**
     * Avatar
     */
    private String avatar;
    
    /**
     * Creation Time
     */
    private OffsetDateTime createdAt;
    
    /**
     * Update Time
     */
    private OffsetDateTime updatedAt;
    
    /**
     * JWT Token
     */
    private String token;
} 