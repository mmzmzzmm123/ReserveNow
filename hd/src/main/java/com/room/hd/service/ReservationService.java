package com.room.hd.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.room.hd.common.Result;
import com.room.hd.dto.ReservationDTO;
import com.room.hd.entity.Reservation;

import java.util.List;
import java.util.Map;

/**
 * Reservation Service Interface
 */
public interface ReservationService {

    /**
     * Create reservation
     * @param reservationDTO Reservation information DTO
     * @param userId User ID
     * @return Creation result
     */
    Result<?> createReservation(ReservationDTO reservationDTO, Integer userId);

    /**
     * Get user's reservation list
     * @param page Current page number
     * @param pageSize Page size
     * @param status Reservation status, multiple statuses separated by commas
     * @param userId User ID
     * @return Reservation list
     */
    Result<?> getUserReservations(Integer page, Integer pageSize, String status, Integer userId);

    /**
     * Cancel reservation
     * @param reservationId Reservation ID
     * @param cancelReason Cancellation reason
     * @param userId User ID
     * @return Cancellation result
     */
    Result<?> cancelReservation(Integer reservationId, String cancelReason, Integer userId);

    /**
     * Get all future reservations for a table
     * @param tableId Table ID
     * @return Reservation list
     */
    Result<?> getTableFutureReservations(Integer tableId);

    /**
     * Get all reservations for administrator
     * @param page Page number, default 1
     * @param pageSize Items per page, default 10
     * @param status Reservation status, multiple statuses separated by commas (0:Cancelled, 1:Pending, 2:Confirmed, 3:Completed, 4:Rejected)
     * @param restaurantId Restaurant ID, if provided only query reservations for this restaurant
     * @return Map containing total count and reservation list
     */
    Map<String, Object> getAllReservations(Integer page, Integer pageSize, String status, Integer restaurantId);

    /**
     * Get reservations for restaurants managed by restaurant manager
     * @param page Page number, default 1
     * @param pageSize Items per page, default 10
     * @param status Reservation status, multiple statuses separated by commas (0:Cancelled, 1:Pending, 2:Confirmed, 3:Completed, 4:Rejected)
     * @param restaurantId Restaurant ID, if provided only query reservations for this restaurant, which must belong to current manager
     * @param managerId Restaurant manager ID
     * @return Map containing total count and reservation list
     */
    Map<String, Object> getManagerReservations(Integer page, Integer pageSize, String status, Integer restaurantId, Integer managerId);

    /**
     * Confirm reservation
     * @param reservationId Reservation ID
     * @param userId Operator ID
     * @param role Operator role (0:Administrator, 1:Restaurant Manager)
     * @return Operation result
     */
    Result<?> confirmReservation(Integer reservationId, Integer userId, Integer role);

    /**
     * Reject reservation
     * @param reservationId Reservation ID
     * @param reason Rejection reason
     * @param userId Operator ID
     * @param role Operator role (0:Administrator, 1:Restaurant Manager)
     * @return Operation result
     */
    Result<?> rejectReservation(Integer reservationId, String reason, Integer userId, Integer role);

    /**
     * Complete reservation
     * @param reservationId Reservation ID
     * @param userId Operator ID
     * @param role Operator role (0:Administrator, 1:Restaurant Manager)
     * @return Operation result
     */
    Result<?> completeReservation(Integer reservationId, Integer userId, Integer role);

    /**
     * Get reservation list for staff's restaurant
     * @param staffId Staff ID
     * @param page Page number
     * @param pageSize Page size
     * @param status Status (optional)
     * @param restaurantId Restaurant ID (optional)
     * @return Reservation list and total count
     */
    Map<String, Object> getStaffReservations(Integer staffId, Integer page, Integer pageSize, String status, Integer restaurantId);

    /**
     * Update reservation status for staff's restaurant
     * @param staffId Staff ID
     * @param reservationId Reservation ID
     * @param status New status
     * @return Updated reservation information
     */
    Reservation updateStaffReservationStatus(Integer staffId, Integer reservationId, Integer status);
} 