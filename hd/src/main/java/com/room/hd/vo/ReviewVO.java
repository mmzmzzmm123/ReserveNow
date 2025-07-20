package com.room.hd.vo;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Review VO Object
 */
@Data
public class ReviewVO {
    
    /**
     * Review ID
     */
    private Integer id;
    
    /**
     * User ID
     */
    private Integer userId;
    
    /**
     * Username
     */
    private String userName;
    
    /**
     * User Avatar
     */
    private String userAvatar;
    
    /**
     * Review Content
     */
    private String content;
    
    /**
     * List of Review Photo URLs
     */
    private List<String> photos;
    
    /**
     * List of Review Video URLs
     */
    private List<String> videos;
    
    /**
     * Rating (1-5)
     */
    private Integer rating;
    
    /**
     * Creation Time
     */
    private OffsetDateTime createdAt;
} 