package com.room.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.room.backend.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Reservation Mapper Interface
 */
@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
    
    /**
     * Get all future reservations for a table
     * @param tableId Table ID
     * @param now Current time
     * @return Reservation list
     */
    @Select("SELECT * FROM reservations WHERE table_id = #{tableId} AND reservation_time >= #{now} AND status IN (0, 1, 2) ORDER BY reservation_time ASC")
    List<Reservation> getFutureReservationsByTableId(@Param("tableId") Integer tableId, @Param("now") OffsetDateTime now);
    
    /**
     * Get all reservations with pagination (for administrator)
     * @param offset Offset
     * @param pageSize Page size
     * @param statusList Status list, e.g., "1,2,3"
     * @param restaurantId Restaurant ID (optional)
     * @return Pagination result
     */
    @Select("<script>"
            + "SELECT r.* FROM reservations r "
            + "<where> 1=1 "
            + "<if test='restaurantId != null'> AND r.restaurant_id = #{restaurantId} </if>"
            + "<if test='statusList != null and statusList.size() > 0'> AND r.status IN "
            + "<foreach collection='statusList' item='status' open='(' separator=',' close=')'> #{status} </foreach>"
            + "</if>"
            + "</where>"
            + " ORDER BY r.created_at DESC"
            + " LIMIT #{pageSize} OFFSET #{offset}"
            + "</script>")
    List<Reservation> getAllReservationsWithPage(
            @Param("offset") int offset, 
            @Param("pageSize") int pageSize,
            @Param("statusList") List<Integer> statusList, 
            @Param("restaurantId") Integer restaurantId);
    
    /**
     * Get total number of all reservations (for administrator)
     * @param statusList Status list
     * @param restaurantId Restaurant ID (optional)
     * @return Total count
     */
    @Select("<script>"
            + "SELECT COUNT(*) FROM reservations "
            + "<where> 1=1 "
            + "<if test='restaurantId != null'> AND restaurant_id = #{restaurantId} </if>"
            + "<if test='statusList != null and statusList.size() > 0'> AND status IN "
            + "<foreach collection='statusList' item='status' open='(' separator=',' close=')'> #{status} </foreach>"
            + "</if>"
            + "</where>"
            + "</script>")
    Long countAllReservations(@Param("statusList") List<Integer> statusList, 
            @Param("restaurantId") Integer restaurantId);
    
    /**
     * Get restaurant manager's reservations with pagination
     * @param offset Offset
     * @param pageSize Page size
     * @param statusList Status list, e.g., "1,2,3"
     * @param restaurantIds Restaurant ID list
     * @return Pagination result
     */
    @Select("<script>"
            + "SELECT r.* FROM reservations r "
            + "<where> 1=1 "
            + "<if test='restaurantIds != null and restaurantIds.size() > 0'> AND r.restaurant_id IN "
            + "<foreach collection='restaurantIds' item='restaurantId' open='(' separator=',' close=')'> #{restaurantId} </foreach>"
            + "</if>"
            + "<if test='statusList != null and statusList.size() > 0'> AND r.status IN "
            + "<foreach collection='statusList' item='status' open='(' separator=',' close=')'> #{status} </foreach>"
            + "</if>"
            + "</where>"
            + " ORDER BY r.created_at DESC"
            + " LIMIT #{pageSize} OFFSET #{offset}"
            + "</script>")
    List<Reservation> getManagerReservationsWithPage(
            @Param("offset") int offset, 
            @Param("pageSize") int pageSize,
            @Param("statusList") List<Integer> statusList, 
            @Param("restaurantIds") List<Integer> restaurantIds);
    
    /**
     * Get total number of restaurant manager's reservations
     * @param statusList Status list
     * @param restaurantIds Restaurant ID list
     * @return Total count
     */
    @Select("<script>"
            + "SELECT COUNT(*) FROM reservations "
            + "<where> 1=1 "
            + "<if test='restaurantIds != null and restaurantIds.size() > 0'> AND restaurant_id IN "
            + "<foreach collection='restaurantIds' item='restaurantId' open='(' separator=',' close=')'> #{restaurantId} </foreach>"
            + "</if>"
            + "<if test='statusList != null and statusList.size() > 0'> AND status IN "
            + "<foreach collection='statusList' item='status' open='(' separator=',' close=')'> #{status} </foreach>"
            + "</if>"
            + "</where>"
            + "</script>")
    Long countManagerReservations(@Param("statusList") List<Integer> statusList, 
            @Param("restaurantIds") List<Integer> restaurantIds);
    
    /**
     * Count all pending reservations
     */
    @Select("SELECT COUNT(*) FROM reservations WHERE status = 1")
    Long countPendingReservations();
    
    /**
     * Count restaurant manager's pending reservations
     */
    @Select("SELECT COUNT(*) FROM reservations r JOIN restaurants res ON r.restaurant_id = res.id WHERE res.owner_id = #{userId} AND r.status = 1")
    Long countManagerPendingReservations(@Param("userId") Integer userId);
    
    /**
     * Count weekly reservations
     */
    @Select("SELECT COUNT(*) FROM reservations WHERE reservation_time BETWEEN #{startDate} AND #{endDate}")
    Long countWeeklyReservations(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    /**
     * Count restaurant manager's weekly reservations
     */
    @Select("SELECT COUNT(*) FROM reservations r JOIN restaurants res ON r.restaurant_id = res.id WHERE res.owner_id = #{userId} AND r.reservation_time BETWEEN #{startDate} AND #{endDate}")
    Long countManagerWeeklyReservations(@Param("userId") Integer userId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    /**
     * Count total reservations
     */
    @Select("SELECT COUNT(*) FROM reservations")
    Long countTotalReservations();
    
    /**
     * Count restaurant manager's total reservations
     */
    @Select("SELECT COUNT(*) FROM reservations r JOIN restaurants res ON r.restaurant_id = res.id WHERE res.owner_id = #{userId}")
    Long countManagerTotalReservations(@Param("userId") Integer userId);
    
    /**
     * Count daily reservations
     */
    @Select("SELECT DATE(reservation_time) as date, COUNT(*) as count FROM reservations WHERE reservation_time BETWEEN #{startDate} AND #{endDate} GROUP BY DATE(reservation_time)")
    List<Map<String, Object>> countDailyReservations(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    /**
     * Count restaurant manager's daily reservations
     */
    @Select("SELECT DATE(r.reservation_time) as date, COUNT(*) as count FROM reservations r JOIN restaurants res ON r.restaurant_id = res.id WHERE res.owner_id = #{userId} AND r.reservation_time BETWEEN #{startDate} AND #{endDate} GROUP BY DATE(r.reservation_time)")
    List<Map<String, Object>> countManagerDailyReservations(@Param("userId") Integer userId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    /**
     * Count reservations by status
     */
    @Select("SELECT status, COUNT(*) as count FROM reservations GROUP BY status")
    List<Map<String, Object>> countReservationsByStatus();
    
    /**
     * Count restaurant manager's reservations by status
     */
    @Select("SELECT r.status, COUNT(*) as count FROM reservations r JOIN restaurants res ON r.restaurant_id = res.id WHERE res.owner_id = #{userId} GROUP BY r.status")
    List<Map<String, Object>> countManagerReservationsByStatus(@Param("userId") Integer userId);
    
    /**
     * Count daily reservations by status
     */
    @Select("SELECT DATE(reservation_time) as date, status, COUNT(*) as count FROM reservations WHERE reservation_time BETWEEN #{startDate} AND #{endDate} GROUP BY DATE(reservation_time), status")
    List<Map<String, Object>> countDailyReservationsByStatus(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
    
    /**
     * Count restaurant manager's daily reservations by status
     */
    @Select("SELECT DATE(r.reservation_time) as date, r.status, COUNT(*) as count FROM reservations r JOIN restaurants res ON r.restaurant_id = res.id WHERE res.owner_id = #{userId} AND r.reservation_time BETWEEN #{startDate} AND #{endDate} GROUP BY DATE(r.reservation_time), r.status")
    List<Map<String, Object>> countManagerDailyReservationsByStatus(@Param("userId") Integer userId, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);
} 