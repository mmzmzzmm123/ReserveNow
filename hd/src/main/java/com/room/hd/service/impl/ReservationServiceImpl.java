package com.room.hd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.room.hd.common.Result;
import com.room.hd.dto.ReservationDTO;
import com.room.hd.entity.Reservation;
import com.room.hd.entity.Restaurant;
import com.room.hd.entity.RestaurantTable;
import com.room.hd.mapper.ReservationMapper;
import com.room.hd.mapper.RestaurantMapper;
import com.room.hd.mapper.RestaurantTableMapper;
import com.room.hd.mapper.ReviewMapper;
import com.room.hd.service.ReservationService;
import com.room.hd.vo.ReservationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reservation Service Implementation
 */
@Service
@Slf4j
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private RestaurantMapper restaurantMapper;

    @Autowired
    private RestaurantTableMapper restaurantTableMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    // Reservation status constants
    private static final int STATUS_CANCELLED = 0;  // Cancelled
    private static final int STATUS_PENDING = 1;    // Pending
    private static final int STATUS_CONFIRMED = 2;  // Confirmed
    private static final int STATUS_COMPLETED = 3;  // Completed
    private static final int STATUS_REJECTED = 4;   // Rejected

    /**
     * Create a reservation
     *
     * @param reservationDTO Reservation information DTO
     * @param userId         User ID
     * @return Creation result
     */
    @Override
    @Transactional
    public Result<?> createReservation(ReservationDTO reservationDTO, Integer userId) {
        try {
            // Parameter validation
            if (reservationDTO == null) {
                return Result.validateFailed("Reservation information cannot be empty");
            }

            if (reservationDTO.getRestaurantId() == null) {
                return Result.validateFailed("Restaurant ID cannot be empty");
            }

            if (reservationDTO.getTableId() == null) {
                return Result.validateFailed("Table ID cannot be empty");
            }

            if (reservationDTO.getReservationTime() == null) {
                return Result.validateFailed("Reservation time cannot be empty");
            }

            if (reservationDTO.getReservationDate() == null || reservationDTO.getReservationDate() <= 0) {
                return Result.validateFailed("Reservation duration must be greater than 0");
            }

            if (reservationDTO.getPersonCount() == null || reservationDTO.getPersonCount() <= 0) {
                return Result.validateFailed("Number of people must be greater than 0");
            }

            // Check if restaurant exists
            Restaurant restaurant = restaurantMapper.selectById(reservationDTO.getRestaurantId());
            if (restaurant == null) {
                return Result.validateFailed("Restaurant does not exist");
            }

            // Check if restaurant is operating
            if (restaurant.getStatus() != 3) { // 3 indicates operating status
                return Result.validateFailed("This restaurant is not available for reservations");
            }

            // Check if table exists and belongs to the restaurant
            RestaurantTable table = restaurantTableMapper.selectById(reservationDTO.getTableId());
            if (table == null) {
                return Result.validateFailed("Table does not exist");
            }

            if (!table.getRestaurantId().equals(reservationDTO.getRestaurantId())) {
                return Result.validateFailed("Table does not belong to this restaurant");
            }

            // Check if reservation time is valid (cannot reserve past time)
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            if (reservationDTO.getReservationTime().isBefore(now)) {
                return Result.validateFailed("Cannot reserve past time");
            }

            // Create reservation record
            Reservation reservation = new Reservation();
            BeanUtils.copyProperties(reservationDTO, reservation);
            reservation.setUserId(userId);
            reservation.setReservationTime(reservationDTO.getReservationTime());
            reservation.setReservationDate(reservationDTO.getReservationDate());
            reservation.setStatus(STATUS_PENDING); // Initial status is pending
            reservation.setCreatedAt(now);
            reservation.setUpdatedAt(now);

            // Save reservation record
            reservationMapper.insert(reservation);

            // Build return VO
            ReservationVO reservationVO = new ReservationVO();
            BeanUtils.copyProperties(reservation, reservationVO);
            reservationVO.setRestaurantName(restaurant.getName());
            reservationVO.setTableType(table.getType());
            reservationVO.setStatusValue(reservation.getStatus());
            reservationVO.setStatusText(getStatusName(reservation.getStatus()));
            reservationVO.setStatus(getStatusName(reservation.getStatus()));

            return Result.success(reservationVO, "Reservation created successfully");
        } catch (Exception e) {
            log.error("Failed to create reservation", e);
            return Result.error(500, "Failed to create reservation: " + e.getMessage());
        }
    }

    /**
     * Get status name
     *
     * @param status Status code
     * @return Status name
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "Unknown";
        }

        switch (status) {
            case STATUS_CANCELLED:
                return "Cancelled";
            case STATUS_PENDING:
                return "Pending";
            case STATUS_CONFIRMED:
                return "Confirmed";
            case STATUS_COMPLETED:
                return "Completed";
            case STATUS_REJECTED:
                return "Rejected";
            default:
                return "Unknown";
        }
    }

    /**
     * Get user's reservation list
     *
     * @param page     Current page number
     * @param pageSize Page size
     * @param status   Reservation status
     * @param userId   User ID
     * @return Reservation list
     */
    @Override
    public Result<?> getUserReservations(Integer page, Integer pageSize, String status, Integer userId) {
        try {
            // Parameter validation
            if (userId == null) {
                return Result.validateFailed("用户ID不能为空");
            }

            // Set pagination parameters
            Page<Reservation> pageParam = new Page<>(page, pageSize);

            // Build query conditions
            LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Reservation::getUserId, userId);

            // Add status filter conditions
            if (status != null && !status.isEmpty()) {
                // Handle multiple status query
                String[] statusArray = status.split(",");
                List<Integer> statusList = new ArrayList<>();
                for (String s : statusArray) {
                    try {
                        statusList.add(Integer.parseInt(s.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("无效的状态值: {}", s);
                    }
                }

                if (!statusList.isEmpty()) {
                    queryWrapper.in(Reservation::getStatus, statusList);
                }
            }

            // Sort by creation time in descending order
            queryWrapper.orderByDesc(Reservation::getCreatedAt);

            // Query data
            IPage<Reservation> resultPage = reservationMapper.selectPage(pageParam, queryWrapper);

            // Convert to VO
            List<ReservationVO> reservationVOList = new ArrayList<>();
            for (Reservation reservation : resultPage.getRecords()) {
                ReservationVO reservationVO = new ReservationVO();
                BeanUtils.copyProperties(reservation, reservationVO);

                // Query restaurant name
                Restaurant restaurant = restaurantMapper.selectById(reservation.getRestaurantId());
                if (restaurant != null) {
                    reservationVO.setRestaurantName(restaurant.getName());
                }

                // Query table type
                RestaurantTable table = restaurantTableMapper.selectById(reservation.getTableId());
                if (table != null) {
                    reservationVO.setTableType(table.getType());
                }

                // Set status name and value
                reservationVO.setStatusValue(reservation.getStatus());
                reservationVO.setStatusText(getStatusName(reservation.getStatus()));
                reservationVO.setStatus(getStatusName(reservation.getStatus()));

                reservationVO.setReviewed(reservation.getReviewId() != null);

                reservationVOList.add(reservationVO);
            }

            // Build return result
            Map<String, Object> result = new HashMap<>();
            result.put("total", resultPage.getTotal());
            result.put("list", reservationVOList);

            return Result.success(result, "");
        } catch (Exception e) {
            log.error("Failed to get user reservation list", e);
            return Result.error(500, "Failed to get user reservation list: " + e.getMessage());
        }
    }

    /**
     * Cancel reservation
     *
     * @param reservationId Reservation ID
     * @param cancelReason  Cancel reason
     * @param userId        User ID
     * @return Cancel result
     */
    @Override
    @Transactional
    public Result<?> cancelReservation(Integer reservationId, String cancelReason, Integer userId) {
        try {
            // Parameter validation
            if (reservationId == null) {
                return Result.validateFailed("Reservation ID cannot be empty");
            }

            if (userId == null) {
                return Result.validateFailed("User ID cannot be empty");
            }

            // Query reservation information
            Reservation reservation = reservationMapper.selectById(reservationId);
            if (reservation == null) {
                return Result.validateFailed("Reservation does not exist");
            }

            // Verify if it's the current user's reservation
            if (!reservation.getUserId().equals(userId)) {
                return Result.forbidden();
            }

            // Check reservation status, only pending and confirmed reservations can be cancelled
            if (reservation.getStatus() != STATUS_PENDING && reservation.getStatus() != STATUS_CONFIRMED) {
                return Result.validateFailed("Cannot cancel reservation in current status");
            }

            // Update reservation status to cancelled
            reservation.setStatus(STATUS_CANCELLED);
            reservation.setCancelReason(cancelReason);
            reservation.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

            // Save update
            reservationMapper.updateById(reservation);

            // Build return data
            Map<String, Object> result = new HashMap<>();
            result.put("id", reservation.getId());
            result.put("status", STATUS_CANCELLED);
            result.put("statusText", getStatusName(STATUS_CANCELLED));
            result.put("cancelReason", reservation.getCancelReason());
            result.put("updatedAt", reservation.getUpdatedAt());

            return Result.success(result, "Cancelled successfully");
        } catch (Exception e) {
            log.error("Failed to cancel reservation", e);
            return Result.error(500, "Failed to cancel reservation: " + e.getMessage());
        }
    }

    /**
     * Get all future reservations for a table
     *
     * @param tableId Table ID
     * @return Reservation list
     */
    @Override
    public Result<?> getTableFutureReservations(Integer tableId) {
        try {
            // Parameter validation
            if (tableId == null) {
                return Result.validateFailed("Table ID cannot be empty");
            }

            // Check if table exists
            RestaurantTable table = restaurantTableMapper.selectById(tableId);
            if (table == null) {
                return Result.validateFailed("Table does not exist");
            }

            // Get current time
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

            // Query future reservations
            List<Reservation> reservations = reservationMapper.getFutureReservationsByTableId(tableId, now);

            // Convert to VO
            List<ReservationVO> reservationVOList = new ArrayList<>();
            for (Reservation reservation : reservations) {
                ReservationVO reservationVO = new ReservationVO();
                BeanUtils.copyProperties(reservation, reservationVO);

                // Query restaurant name
                Restaurant restaurant = restaurantMapper.selectById(reservation.getRestaurantId());
                if (restaurant != null) {
                    reservationVO.setRestaurantName(restaurant.getName());
                }

                // Set table type
                reservationVO.setTableType(table.getType());

                // Set status name and value
                reservationVO.setStatusValue(reservation.getStatus());
                reservationVO.setStatusText(getStatusName(reservation.getStatus()));
                reservationVO.setStatus(getStatusName(reservation.getStatus()));

                reservationVOList.add(reservationVO);
            }

            return Result.success(reservationVOList, "Retrieved successfully");
        } catch (Exception e) {
            log.error("Failed to get table future reservations", e);
            return Result.error(500, "Failed to get table future reservations: " + e.getMessage());
        }
    }

    /**
     * Admin gets all reservation list
     *
     * @param page         Current page number
     * @param pageSize     Page size
     * @param status       Reservation status, multiple statuses separated by commas
     * @param restaurantId Restaurant ID, optional filter condition
     * @return Reservation list
     */
    @Override
    public Map<String, Object> getAllReservations(Integer page, Integer pageSize, String status, Integer restaurantId) {
        try {
            // Parameter validation
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }

            // Process status parameter
            List<Integer> statusList = new ArrayList<>();
            if (status != null && !status.isEmpty()) {
                String[] statusArray = status.split(",");
                for (String s : statusArray) {
                    try {
                        statusList.add(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid status value: {}", s);
                    }
                }
            }

            // Calculate offset
            int offset = (page - 1) * pageSize;

            // Use custom SQL for paginated query
            List<Reservation> reservations = reservationMapper.getAllReservationsWithPage(
                    offset,
                    pageSize,
                    statusList.isEmpty() ? null : statusList,
                    restaurantId);

            // Query total record count
            Long total = reservationMapper.countAllReservations(
                    statusList.isEmpty() ? null : statusList,
                    restaurantId);

            // Get restaurant and table information for reservations
            List<ReservationVO> reservationVOList = new ArrayList<>();
            for (Reservation reservation : reservations) {
                ReservationVO reservationVO = convertToReservationVO(reservation);
                reservationVOList.add(reservationVO);
            }

            // Build return result
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", reservationVOList);

            return result;
        } catch (Exception e) {
            log.error("Failed to get all reservations: ", e);
            throw new RuntimeException("Failed to get reservation list: " + e.getMessage());
        }
    }

    /**
     * Restaurant manager gets all reservations for their restaurant
     *
     * @param page         Current page number
     * @param pageSize     Page size
     * @param status       Reservation status, multiple statuses separated by commas
     * @param restaurantId Restaurant ID, optional filter condition
     * @param managerId    Restaurant manager ID
     * @return Reservation list
     */
    @Override
    public Map<String, Object> getManagerReservations(Integer page, Integer pageSize, String status, Integer restaurantId, Integer managerId) {
        try {
            // Parameter validation
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }
            if (managerId == null) {
                throw new IllegalArgumentException("Restaurant manager ID cannot be empty");
            }

            // Query restaurants managed by this manager
            LambdaQueryWrapper<Restaurant> restaurantQuery = new LambdaQueryWrapper<>();
            restaurantQuery.eq(Restaurant::getOwnerId, managerId);

            // If restaurantId is specified, check if it belongs to this manager
            if (restaurantId != null) {
                restaurantQuery.eq(Restaurant::getId, restaurantId);
            }

            List<Restaurant> restaurants = restaurantMapper.selectList(restaurantQuery);

            if (restaurants.isEmpty()) {
                // No restaurants or specified restaurant does not belong to this manager
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("total", 0);
                emptyResult.put("list", new ArrayList<>());
                return emptyResult;
            }

            // Get all restaurant IDs
            List<Integer> restaurantIds = restaurants.stream()
                    .map(Restaurant::getId)
                    .collect(Collectors.toList());

            // Process status parameter
            List<Integer> statusList = new ArrayList<>();
            if (status != null && !status.isEmpty()) {
                String[] statusArray = status.split(",");
                for (String s : statusArray) {
                    try {
                        statusList.add(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid status value: {}", s);
                    }
                }
            }

            // Calculate offset
            int offset = (page - 1) * pageSize;

            // Use custom SQL for paginated query
            List<Integer> finalRestaurantIds = new ArrayList<>();
            if (restaurantId != null) {
                finalRestaurantIds.add(restaurantId);
            } else {
                finalRestaurantIds.addAll(restaurantIds);
            }

            List<Reservation> reservations = reservationMapper.getManagerReservationsWithPage(
                    offset,
                    pageSize,
                    statusList.isEmpty() ? null : statusList,
                    finalRestaurantIds);

            // Query total record count
            Long total = reservationMapper.countManagerReservations(
                    statusList.isEmpty() ? null : statusList,
                    finalRestaurantIds);

            // Get restaurant and table information for reservations
            List<ReservationVO> reservationVOList = new ArrayList<>();
            for (Reservation reservation : reservations) {
                ReservationVO reservationVO = convertToReservationVO(reservation);
                reservationVOList.add(reservationVO);
            }

            // Build return result
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", reservationVOList);

            return result;
        } catch (Exception e) {
            log.error("Failed to get manager reservations: ", e);
            throw new RuntimeException("Failed to get reservation list: " + e.getMessage());
        }
    }

    /**
     * Convert Reservation to ReservationVO
     *
     * @param reservation Reservation entity
     * @return Reservation VO
     */
    private ReservationVO convertToReservationVO(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        ReservationVO reservationVO = new ReservationVO();
        BeanUtils.copyProperties(reservation, reservationVO);

        // Get restaurant name
        if (reservation.getRestaurantId() != null) {
            Restaurant restaurant = restaurantMapper.selectById(reservation.getRestaurantId());
            if (restaurant != null) {
                reservationVO.setRestaurantName(restaurant.getName());
            }
        }

        // Get table type
        if (reservation.getTableId() != null) {
            RestaurantTable table = restaurantTableMapper.selectById(reservation.getTableId());
            if (table != null) {
                reservationVO.setTableType(table.getType());
            }
        }

        // Set status text
        reservationVO.setStatusValue(reservation.getStatus());
        reservationVO.setStatusText(getStatusName(reservation.getStatus()));
        reservationVO.setStatus(getStatusName(reservation.getStatus()));

        // Check if it has been reviewed
        if (reservation.getUserId() != null && reservation.getRestaurantId() != null) {
            Integer reviewCount = reviewMapper.checkReviewExistence(reservation.getUserId(), reservation.getRestaurantId());
            reservationVO.setReviewed(reviewCount != null && reviewCount > 0);
        } else {
            reservationVO.setReviewed(false);
        }

        return reservationVO;
    }

    /**
     * Confirm reservation
     *
     * @param reservationId Reservation ID
     * @param userId        Operator ID
     * @param role          Operator role(0: Admin, 1: Restaurant Manager)
     * @return Operation result
     */
    @Override
    @Transactional
    public Result<?> confirmReservation(Integer reservationId, Integer userId, Integer role) {
        try {
            // Parameter validation
            if (reservationId == null) {
                return Result.validateFailed("Reservation ID cannot be empty");
            }

            // Query reservation information
            Reservation reservation = reservationMapper.selectById(reservationId);
            if (reservation == null) {
                return Result.validateFailed("Reservation does not exist");
            }

            // Verify permissions, admin can handle all reservations, manager can only handle their restaurant's reservations
            if (role == 1) { // Restaurant Manager
                // Query if the restaurant belongs to this manager
                LambdaQueryWrapper<Restaurant> restaurantQuery = new LambdaQueryWrapper<>();
                restaurantQuery.eq(Restaurant::getId, reservation.getRestaurantId())
                        .eq(Restaurant::getOwnerId, userId);
                if (restaurantMapper.selectCount(restaurantQuery) == 0) {
                    return Result.forbidden("No permission to handle this reservation");
                }
            }

            // Check reservation status, only pending reservations can be confirmed
            if (reservation.getStatus() != STATUS_PENDING) {
                return Result.validateFailed("Cannot confirm reservation in current status");
            }

            // Update reservation status to confirmed
            reservation.setStatus(STATUS_CONFIRMED);
            reservation.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

            // Save update
            reservationMapper.updateById(reservation);

            // Build return data
            Map<String, Object> result = new HashMap<>();
            result.put("id", reservation.getId());
            result.put("status", STATUS_CONFIRMED);
            result.put("statusText", getStatusName(STATUS_CONFIRMED));
            result.put("updatedAt", reservation.getUpdatedAt());

            return Result.success(result, "Confirmed successfully");
        } catch (Exception e) {
            log.error("Failed to confirm reservation", e);
            return Result.error(500, "Failed to confirm reservation: " + e.getMessage());
        }
    }

    /**
     * Reject reservation
     *
     * @param reservationId Reservation ID
     * @param reason        Rejection reason
     * @param userId        Operator ID
     * @param role          Operator role(0: Admin, 1: Restaurant Manager)
     * @return Operation result
     */
    @Override
    @Transactional
    public Result<?> rejectReservation(Integer reservationId, String reason, Integer userId, Integer role) {
        try {
            // Parameter validation
            if (reservationId == null) {
                return Result.validateFailed("Reservation ID cannot be empty");
            }

            if (reason == null || reason.trim().isEmpty()) {
                return Result.validateFailed("Rejection reason cannot be empty");
            }

            // Query reservation information
            Reservation reservation = reservationMapper.selectById(reservationId);
            if (reservation == null) {
                return Result.validateFailed("Reservation does not exist");
            }

            // Verify permissions, admin can handle all reservations, manager can only handle their restaurant's reservations
            if (role == 1) { // Restaurant Manager
                // Query if the restaurant belongs to this manager
                LambdaQueryWrapper<Restaurant> restaurantQuery = new LambdaQueryWrapper<>();
                restaurantQuery.eq(Restaurant::getId, reservation.getRestaurantId())
                        .eq(Restaurant::getOwnerId, userId);
                if (restaurantMapper.selectCount(restaurantQuery) == 0) {
                    return Result.forbidden("No permission to handle this reservation");
                }
            }

            // Check reservation status, only pending reservations can be rejected
            if (reservation.getStatus() != STATUS_PENDING) {
                return Result.validateFailed("Cannot reject reservation in current status");
            }

            // Update reservation status to rejected
            reservation.setStatus(STATUS_REJECTED);
            reservation.setCancelReason(reason);
            reservation.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

            // Save update
            reservationMapper.updateById(reservation);

            // Build return data
            Map<String, Object> result = new HashMap<>();
            result.put("id", reservation.getId());
            result.put("status", STATUS_REJECTED);
            result.put("statusText", getStatusName(STATUS_REJECTED));
            result.put("rejectReason", reservation.getCancelReason());
            result.put("updatedAt", reservation.getUpdatedAt());

            return Result.success(result, "Rejected successfully");
        } catch (Exception e) {
            log.error("Failed to reject reservation", e);
            return Result.error(500, "Failed to reject reservation: " + e.getMessage());
        }
    }

    /**
     * Complete reservation
     *
     * @param reservationId Reservation ID
     * @param userId        Operator ID
     * @param role          Operator role(0: Admin, 1: Restaurant Manager)
     * @return Operation result
     */
    @Override
    @Transactional
    public Result<?> completeReservation(Integer reservationId, Integer userId, Integer role) {
        try {
            // Parameter validation
            if (reservationId == null) {
                return Result.validateFailed("Reservation ID cannot be empty");
            }

            // Query reservation information
            Reservation reservation = reservationMapper.selectById(reservationId);
            if (reservation == null) {
                return Result.validateFailed("Reservation does not exist");
            }

            // Verify permissions, admin can handle all reservations, manager can only handle their restaurant's reservations
            if (role == 1) { // Restaurant Manager
                // Query if the restaurant belongs to this manager
                LambdaQueryWrapper<Restaurant> restaurantQuery = new LambdaQueryWrapper<>();
                restaurantQuery.eq(Restaurant::getId, reservation.getRestaurantId())
                        .eq(Restaurant::getOwnerId, userId);
                if (restaurantMapper.selectCount(restaurantQuery) == 0) {
                    return Result.forbidden("No permission to handle this reservation");
                }
            }

            // Check reservation status, only confirmed reservations can be completed
            if (reservation.getStatus() != STATUS_CONFIRMED) {
                return Result.validateFailed("Cannot complete reservation in current status");
            }

            // Update reservation status to completed
            reservation.setStatus(STATUS_COMPLETED);
            reservation.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

            // Save update
            reservationMapper.updateById(reservation);

            // Build return data
            Map<String, Object> result = new HashMap<>();
            result.put("id", reservation.getId());
            result.put("status", STATUS_COMPLETED);
            result.put("statusText", getStatusName(STATUS_COMPLETED));
            result.put("updatedAt", reservation.getUpdatedAt());

            return Result.success(result, "Completed successfully");
        } catch (Exception e) {
            log.error("Failed to complete reservation", e);
            return Result.error(500, "Failed to complete reservation: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getStaffReservations(Integer staffId, Integer page, Integer pageSize, String status, Integer restaurantId) {
        try {
            // Get list of restaurant IDs where staff works
            List<Integer> restaurantIds = restaurantMapper.getRestaurantIdsByStaffId(staffId);
            if (restaurantIds.isEmpty()) {
                return new HashMap<String, Object>() {{
                    put("total", 0L);
                    put("list", new ArrayList<>());
                }};
            }

            // If restaurantId is specified, verify if staff has access to it
            if (restaurantId != null && !restaurantIds.contains(restaurantId)) {
                throw new IllegalArgumentException("No permission to access reservation information for this restaurant");
            }

            // Process status parameter
            List<Integer> statusList = null;
            if (status != null && !status.isEmpty()) {
                statusList = new ArrayList<>();
                for (String s : status.split(",")) {
                    statusList.add(Integer.parseInt(s.trim()));
                }
            }

            // Calculate pagination parameters
            int offset = (page - 1) * pageSize;

            // Get reservation list
            List<Reservation> reservations;
            Long total;
            if (restaurantId != null) {
                reservations = reservationMapper.getManagerReservationsWithPage(offset, pageSize, statusList, List.of(restaurantId));
                total = reservationMapper.countManagerReservations(statusList, List.of(restaurantId));
            } else {
                reservations = reservationMapper.getManagerReservationsWithPage(offset, pageSize, statusList, restaurantIds);
                total = reservationMapper.countManagerReservations(statusList, restaurantIds);
            }

            // Convert to ReservationVO list
            List<ReservationVO> reservationVOList = new ArrayList<>();
            for (Reservation reservation : reservations) {
                ReservationVO reservationVO = convertToReservationVO(reservation);
                reservationVOList.add(reservationVO);
            }

            // Build return result
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", reservationVOList);

            return result;
        } catch (Exception e) {
            log.error("Failed to get staff reservation list", e);
            throw new RuntimeException("Failed to get staff reservation list: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reservation updateStaffReservationStatus(Integer staffId, Integer reservationId, Integer status) {
        try {
            // Get reservation information
            Reservation reservation = reservationMapper.selectById(reservationId);
            if (reservation == null) {
                throw new IllegalArgumentException("Reservation does not exist");
            }

            // Verify if staff has permission to modify this reservation
            List<Integer> restaurantIds = restaurantMapper.getRestaurantIdsByStaffId(staffId);
            if (!restaurantIds.contains(reservation.getRestaurantId())) {
                throw new IllegalArgumentException("No permission to modify this reservation status");
            }

            // Update reservation status
            reservation.setStatus(status);
            int rows = reservationMapper.updateById(reservation);
            if (rows != 1) {
                throw new RuntimeException("Failed to update reservation status");
            }

            return reservation;
        } catch (Exception e) {
            log.error("Failed to update reservation status", e);
            throw new RuntimeException("Failed to update reservation status: " + e.getMessage());
        }
    }
} 