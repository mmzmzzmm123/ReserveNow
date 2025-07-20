package com.room.hd.service;

import com.room.hd.common.Result;
import com.room.hd.dto.StaffApplyDTO;
import com.room.hd.entity.Staff;
import java.util.Map;

/**
 * Staff Service Interface
 */
public interface StaffService {
    
    /**
     * Get manager's staff list
     * @param managerId Manager ID
     * @param page Page number
     * @param pageSize Page size
     * @return Staff list and total count
     */
    Map<String, Object> getManagerStaffList(Integer managerId, int page, int pageSize);
    
    /**
     * Review staff application
     * @param staffId Staff ID
     * @param managerId Manager ID
     * @param status Status (0:Pending, 1:Approved)
     * @return Success status
     */
    boolean updateStaffStatus(Integer staffId, Integer managerId, Integer status);
    
    /**
     * Delete staff
     * @param staffId Staff ID
     * @param managerId Manager ID
     * @return Success status
     */
    boolean deleteStaff(Integer staffId, Integer managerId);

    /**
     * Get restaurant manager list
     * @param page Page number
     * @param pageSize Page size
     * @param keyword Search keyword (name or email)
     * @return Manager list and total count
     */
    Map<String, Object> getManagerList(int page, int pageSize, String keyword);
    
    /**
     * Apply to become a staff member
     * @param staffApplyDTO Application information
     * @return Application result
     */
    Result<?> applyForStaff(StaffApplyDTO staffApplyDTO);
}