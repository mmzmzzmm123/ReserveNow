package com.room.hd.service;

import com.room.hd.common.Result;
import com.room.hd.dto.StatisticsDTO;

import java.util.Map;

/**
 * Administrator Service Interface
 */
public interface AdminService {
    
    /**
     * Get system statistics
     * @param userInfo User information, including role and ID
     * @return Statistics data
     */
    Result<StatisticsDTO> getStatistics(Map<String, Object> userInfo);
    
    /**
     * Get reservation statistics for the last 7 days
     * @param userInfo User information, including role and ID
     * @return Reservation statistics for the last 7 days
     */
    Result<Map<String, Object>> getReservationStatistics(Map<String, Object> userInfo);
    
    /**
     * Get restaurant statistics for the last 7 days
     * @param userInfo User information, including role and ID
     * @return Restaurant statistics for the last 7 days
     */
    Result<Map<String, Object>> getRestaurantStatistics(Map<String, Object> userInfo);
} 