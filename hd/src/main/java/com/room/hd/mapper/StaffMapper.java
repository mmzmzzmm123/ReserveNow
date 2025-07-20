package com.room.hd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.room.hd.entity.Staff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Staff Mapper Interface
 */
@Mapper
public interface StaffMapper extends BaseMapper<Staff> {
    
    /**
     * Get manager's staff list (including user information)
     * @param managerId Manager ID
     * @param offset Offset
     * @param pageSize Page size
     * @return Staff list
     */
    @Select("SELECT s.*, u.name as user_name, u.email as user_email " +
            "FROM staff s " +
            "LEFT JOIN users u ON s.user_id = u.id " +
            "WHERE s.manager_id = #{managerId} " +
            "ORDER BY s.created_at DESC " +
            "LIMIT #{pageSize} OFFSET #{offset}")
    List<Staff> getManagerStaffList(
            @Param("managerId") Integer managerId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);
    
    /**
     * Get total number of manager's staff
     * @param managerId Manager ID
     * @return Total number of staff
     */
    @Select("SELECT COUNT(*) FROM staff WHERE manager_id = #{managerId}")
    Long countManagerStaff(@Param("managerId") Integer managerId);
} 