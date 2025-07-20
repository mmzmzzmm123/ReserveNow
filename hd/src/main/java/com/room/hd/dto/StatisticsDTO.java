package com.room.hd.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Statistics Data DTO
 */
@Data
public class StatisticsDTO {
    
    /**
     * Total number of restaurants
     */
    private Long totalRestaurants;
    
    /**
     * Number of pending reservations
     */
    private Long pendingReservations;
    
    /**
     * Number of weekly reservations
     */
    private Long weeklyReservations;
    
    /**
     * Total number of reservations
     */
    private Long totalReservations;
    
    /**
     * Daily reservation counts for the last 7 days
     */
    private List<DailyStatisticsDTO> dailyReservations;
    
    /**
     * Reservation counts by status
     */
    private Map<String, Long> reservationStatusCount;
    
    /**
     * Daily restaurant counts for the last 7 days
     */
    private List<DailyStatisticsDTO> dailyRestaurants;
    
    /**
     * Restaurant counts by status
     */
    private Map<String, Long> restaurantStatusCount;
    
    /**
     * Daily Statistics Data
     */
    @Data
    public static class DailyStatisticsDTO {
        /**
         * Date (format: yyyy-MM-dd)
         */
        private String date;
        
        /**
         * Count
         */
        private Long count;
        
        /**
         * Status distribution
         */
        private Map<String, Long> statusCount;
    }
} 