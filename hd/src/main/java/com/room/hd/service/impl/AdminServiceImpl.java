package com.room.hd.service.impl;

import com.room.hd.common.Result;
import com.room.hd.dto.StatisticsDTO;
import com.room.hd.mapper.ReservationMapper;
import com.room.hd.mapper.RestaurantMapper;
import com.room.hd.service.AdminService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin Service Implementation
 */
@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    @Resource
    private ReservationMapper reservationMapper;

    @Resource
    private RestaurantMapper restaurantMapper;

    // Status name mapping
    private static final Map<Integer, String> RESERVATION_STATUS_MAP = new HashMap<>();
    private static final Map<Integer, String> RESTAURANT_STATUS_MAP = new HashMap<>();

    static {
        // Reservation status mapping
        RESERVATION_STATUS_MAP.put(0, "Cancelled");
        RESERVATION_STATUS_MAP.put(1, "Pending");
        RESERVATION_STATUS_MAP.put(2, "Confirmed");
        RESERVATION_STATUS_MAP.put(3, "Completed");
        RESERVATION_STATUS_MAP.put(4, "Rejected");

        // Restaurant status mapping
        RESTAURANT_STATUS_MAP.put(0, "Pending Review");
        RESTAURANT_STATUS_MAP.put(1, "Approved");
        RESTAURANT_STATUS_MAP.put(2, "Closed");
        RESTAURANT_STATUS_MAP.put(3, "Operating");
    }

    @Override
    public Result<StatisticsDTO> getStatistics(Map<String, Object> userInfo) {
        try {
            Integer role = (Integer) userInfo.get("role");
            Integer userId = (Integer) userInfo.get("userId");

            StatisticsDTO statisticsDTO = new StatisticsDTO();

            // Get different statistics based on role
            if (role == 0) { // Admin
                // Get total number of all restaurants
                Long totalRestaurants = restaurantMapper.countTotalRestaurants();
                statisticsDTO.setTotalRestaurants(totalRestaurants);

                // Get total number of pending reservations
                Long pendingReservations = reservationMapper.countPendingReservations();
                statisticsDTO.setPendingReservations(pendingReservations);

                // Get total number of weekly reservations
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                LocalDate today = now.toLocalDate();
                LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

                OffsetDateTime startDate = startOfWeek.atStartOfDay().atOffset(ZoneOffset.UTC);
                OffsetDateTime endDate = endOfWeek.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

                Long weeklyReservations = reservationMapper.countWeeklyReservations(startDate, endDate);
                statisticsDTO.setWeeklyReservations(weeklyReservations);

                // Get total number of all reservations
                Long totalReservations = reservationMapper.countTotalReservations();
                statisticsDTO.setTotalReservations(totalReservations);
            } else { // Restaurant Manager
                // Get total number of their restaurants
                Long totalRestaurants = restaurantMapper.countManagerRestaurants(userId);
                statisticsDTO.setTotalRestaurants(totalRestaurants);

                // Get total number of pending reservations for their restaurants
                Long pendingReservations = reservationMapper.countManagerPendingReservations(userId);
                statisticsDTO.setPendingReservations(pendingReservations);

                // Get total number of weekly reservations for their restaurants
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                LocalDate today = now.toLocalDate();
                LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

                OffsetDateTime startDate = startOfWeek.atStartOfDay().atOffset(ZoneOffset.UTC);
                OffsetDateTime endDate = endOfWeek.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

                Long weeklyReservations = reservationMapper.countManagerWeeklyReservations(userId, startDate, endDate);
                statisticsDTO.setWeeklyReservations(weeklyReservations);

                // Get total number of all reservations for their restaurants
                Long totalReservations = reservationMapper.countManagerTotalReservations(userId);
                statisticsDTO.setTotalReservations(totalReservations);
            }

            return Result.success(statisticsDTO, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get statistics", e);
            return Result.error(500, "Failed to get statistics: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> getReservationStatistics(Map<String, Object> userInfo) {
        try {
            Integer role = (Integer) userInfo.get("role");
            Integer userId = (Integer) userInfo.get("userId");

            Map<String, Object> result = new HashMap<>();

            // Get date range for last 7 days
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            LocalDate today = now.toLocalDate();
            LocalDate sevenDaysAgo = today.minusDays(6);

            OffsetDateTime startDate = sevenDaysAgo.atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime endDate = today.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

            // Get different statistics based on role
            List<Map<String, Object>> dailyReservationsList;
            List<Map<String, Object>> statusCountList;
            List<Map<String, Object>> dailyStatusCountList;

            if (role == 0) { // Admin
                dailyReservationsList = reservationMapper.countDailyReservations(startDate, endDate);
                statusCountList = reservationMapper.countReservationsByStatus();
                dailyStatusCountList = reservationMapper.countDailyReservationsByStatus(startDate, endDate);
            } else { // Restaurant Manager
                dailyReservationsList = reservationMapper.countManagerDailyReservations(userId, startDate, endDate);
                statusCountList = reservationMapper.countManagerReservationsByStatus(userId);
                dailyStatusCountList = reservationMapper.countManagerDailyReservationsByStatus(userId, startDate, endDate);
            }

            List<StatisticsDTO.DailyStatisticsDTO> dailyReservations = new ArrayList<>();

            Map<String, Long> dateCountMap = dailyReservationsList.stream()
                    .collect(Collectors.toMap(
                            m -> m.get("date").toString(),
                            m -> Long.parseLong(m.get("count").toString()),
                            (a, b) -> a
                    ));

            Map<String, Long> statusCount = new HashMap<>();
            for (Map<String, Object> item : statusCountList) {
                Integer status = Integer.parseInt(item.get("status").toString());
                String statusName = RESERVATION_STATUS_MAP.getOrDefault(status, "Unknown");
                Long count = Long.parseLong(item.get("count").toString());
                statusCount.put(statusName, count);
            }

            Map<String, Map<String, Long>> dateStatusMap = new HashMap<>();
            for (Map<String, Object> item : dailyStatusCountList) {
                String date = item.get("date").toString();
                Integer status = Integer.parseInt(item.get("status").toString());
                String statusName = RESERVATION_STATUS_MAP.getOrDefault(status, "Unknown");
                Long count = Long.parseLong(item.get("count").toString());

                dateStatusMap.computeIfAbsent(date, k -> new HashMap<>());
                dateStatusMap.get(date).put(statusName, count);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (int i = 0; i < 7; i++) {
                LocalDate date = sevenDaysAgo.plusDays(i);
                String dateStr = date.format(formatter);

                StatisticsDTO.DailyStatisticsDTO dailyDTO = new StatisticsDTO.DailyStatisticsDTO();
                dailyDTO.setDate(dateStr);
                dailyDTO.setCount(dateCountMap.getOrDefault(dateStr, 0L));
                dailyDTO.setStatusCount(dateStatusMap.getOrDefault(dateStr, new HashMap<>()));

                dailyReservations.add(dailyDTO);
            }

            result.put("dailyReservations", dailyReservations);
            result.put("statusCount", statusCount);

            return Result.success(result, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get reservation statistics", e);
            return Result.error(500, "Failed to get reservation statistics: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> getRestaurantStatistics(Map<String, Object> userInfo) {
        try {
            Integer role = (Integer) userInfo.get("role");
            Integer userId = (Integer) userInfo.get("userId");

            Map<String, Object> result = new HashMap<>();

            // Get date range for last 7 days
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            LocalDate today = now.toLocalDate();
            LocalDate sevenDaysAgo = today.minusDays(6);

            OffsetDateTime startDate = sevenDaysAgo.atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime endDate = today.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

            // Get different statistics based on role
            List<Map<String, Object>> dailyRestaurantsList;
            List<Map<String, Object>> statusCountList;
            List<Map<String, Object>> dailyStatusCountList;

            if (role == 0) { // Admin
                dailyRestaurantsList = restaurantMapper.countDailyRestaurants(startDate, endDate);
                statusCountList = restaurantMapper.countRestaurantsByStatus();
                dailyStatusCountList = restaurantMapper.countDailyRestaurantsByStatus(startDate, endDate);
            } else { // Restaurant Manager
                dailyRestaurantsList = restaurantMapper.countManagerDailyRestaurants(userId, startDate, endDate);
                statusCountList = restaurantMapper.countManagerRestaurantsByStatus(userId);
                dailyStatusCountList = restaurantMapper.countManagerDailyRestaurantsByStatus(userId, startDate, endDate);
            }

            List<StatisticsDTO.DailyStatisticsDTO> dailyRestaurants = new ArrayList<>();

            Map<String, Long> dateCountMap = dailyRestaurantsList.stream()
                    .collect(Collectors.toMap(
                            m -> m.get("date").toString(),
                            m -> Long.parseLong(m.get("count").toString()),
                            (a, b) -> a
                    ));

            Map<String, Long> statusCount = new HashMap<>();
            for (Map<String, Object> item : statusCountList) {
                Integer status = Integer.parseInt(item.get("status").toString());
                String statusName = RESTAURANT_STATUS_MAP.getOrDefault(status, "Unknown");
                Long count = Long.parseLong(item.get("count").toString());
                statusCount.put(statusName, count);
            }

            Map<String, Map<String, Long>> dateStatusMap = new HashMap<>();
            for (Map<String, Object> item : dailyStatusCountList) {
                String date = item.get("date").toString();
                Integer status = Integer.parseInt(item.get("status").toString());
                String statusName = RESTAURANT_STATUS_MAP.getOrDefault(status, "Unknown");
                Long count = Long.parseLong(item.get("count").toString());

                dateStatusMap.computeIfAbsent(date, k -> new HashMap<>());
                dateStatusMap.get(date).put(statusName, count);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (int i = 0; i < 7; i++) {
                LocalDate date = sevenDaysAgo.plusDays(i);
                String dateStr = date.format(formatter);

                StatisticsDTO.DailyStatisticsDTO dailyDTO = new StatisticsDTO.DailyStatisticsDTO();
                dailyDTO.setDate(dateStr);
                dailyDTO.setCount(dateCountMap.getOrDefault(dateStr, 0L));
                dailyDTO.setStatusCount(dateStatusMap.getOrDefault(dateStr, new HashMap<>()));

                dailyRestaurants.add(dailyDTO);
            }

            result.put("dailyRestaurants", dailyRestaurants);
            result.put("statusCount", statusCount);

            return Result.success(result, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get restaurant statistics", e);
            return Result.error(500, "Failed to get restaurant statistics: " + e.getMessage());
        }
    }
} 