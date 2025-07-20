package com.room.hd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.room.hd.common.Result;
import com.room.hd.dto.StaffApplyDTO;
import com.room.hd.entity.Staff;
import com.room.hd.entity.User;
import com.room.hd.mapper.StaffMapper;
import com.room.hd.mapper.UserMapper;
import com.room.hd.service.StaffService;
import com.room.hd.vo.UserVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements StaffService {
    
    @Resource
    private UserMapper userMapper;
    
    // User role constants
    private static final int ROLE_ADMIN = 0;
    private static final int ROLE_MANAGER = 1;
    private static final int ROLE_USER = 2;
    private static final int ROLE_WAITER = 3;
    
    @Override
    public Map<String, Object> getManagerStaffList(Integer managerId, int page, int pageSize) {
        try {
            int offset = (page - 1) * pageSize;
            List<Staff> staffList = baseMapper.getManagerStaffList(managerId, offset, pageSize);
            Long total = baseMapper.countManagerStaff(managerId);

            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", staffList);
            return result;
        } catch (Exception e) {
            log.error("Failed to get manager's staff list: ", e);
            throw new RuntimeException("Failed to get staff list");
        }
    }
    
    @Override
    @Transactional
    public boolean updateStaffStatus(Integer staffId, Integer managerId, Integer status) {
        try {
            LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Staff::getId, staffId)
                    .eq(Staff::getManagerId, managerId);
            
            Staff staff = getOne(wrapper);
            if (staff == null) {
                throw new RuntimeException("Staff does not exist or no permission to operate");
            }
            
            staff.setStatus(status);
            return updateById(staff);
        } catch (Exception e) {
            log.error("Failed to update staff status: ", e);
            throw new RuntimeException("Failed to update staff status");
        }
    }
    
    @Override
    @Transactional
    public boolean deleteStaff(Integer staffId, Integer managerId) {
        try {
            LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Staff::getId, staffId)
                    .eq(Staff::getManagerId, managerId);
            
            return remove(wrapper);
        } catch (Exception e) {
            log.error("Failed to delete staff: ", e);
            throw new RuntimeException("Failed to delete staff");
        }
    }
    
    @Override
    public Map<String, Object> getManagerList(int page, int pageSize, String keyword) {
        try {
            // Query restaurant manager users
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getRole, ROLE_MANAGER)
                   .eq(User::getStatus, 1); // Only query active managers
            
            // Add keyword search
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.and(w -> w.like(User::getName, keyword)
                               .or()
                               .like(User::getEmail, keyword));
            }
            
            // Calculate pagination parameters
            int offset = (page - 1) * pageSize;
            
            // Get total count
            long total = userMapper.selectCount(wrapper);
            
            // Add pagination and sorting
            wrapper.orderByDesc(User::getCreatedAt)
                   .last("LIMIT " + pageSize + " OFFSET " + offset);
            
            // Execute query
            List<User> managers = userMapper.selectList(wrapper);
            
            // Convert to ManagerVO list
            List<Map<String, Object>> managerList = managers.stream().map(manager -> {
                Map<String, Object> managerMap = new HashMap<>();
                managerMap.put("managerId", manager.getId());
                managerMap.put("name", manager.getName());
                managerMap.put("email", manager.getEmail());
                managerMap.put("avatar", manager.getAvatar());
                managerMap.put("createdAt", manager.getCreatedAt());
                return managerMap;
            }).collect(Collectors.toList());
            
            // Build return result
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", managerList);
            return result;
        } catch (Exception e) {
            log.error("Failed to get manager list: ", e);
            throw new RuntimeException("Failed to get manager list: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<?> applyForStaff(StaffApplyDTO staffApplyDTO) {
        try {
            // Parameter validation
            if (staffApplyDTO.getUserId() == null || staffApplyDTO.getManagerId() == null) {
                return Result.validateFailed("User ID and Manager ID cannot be empty");
            }
            
            // Check if user exists
            User user = userMapper.selectById(staffApplyDTO.getUserId());
            if (user == null) {
                return Result.validateFailed("User does not exist");
            }
            
            // Check if manager exists and is a restaurant manager
            User manager = userMapper.selectById(staffApplyDTO.getManagerId());
            if (manager == null) {
                return Result.validateFailed("Manager does not exist");
            }
            if (manager.getRole() != ROLE_MANAGER) {
                return Result.validateFailed("Specified user is not a restaurant manager");
            }

            // Check if already an employee of another manager
            LambdaQueryWrapper<Staff> checkWrappers = new LambdaQueryWrapper<>();
            checkWrappers.eq(Staff::getUserId, staffApplyDTO.getUserId());
            if (count(checkWrappers) > 0) {
                return Result.validateFailed("You are already an employee of another manager, cannot apply");
            }
            
            // Check if already applied to this manager
            LambdaQueryWrapper<Staff> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(Staff::getUserId, staffApplyDTO.getUserId())
                        .eq(Staff::getManagerId, staffApplyDTO.getManagerId());
            long count = count(checkWrapper);
            if (count > 0) {
                return Result.validateFailed("You have already submitted an application to this manager");
            }
            
            // Create staff application record
            Staff staff = new Staff();
            staff.setUserId(staffApplyDTO.getUserId());
            staff.setManagerId(staffApplyDTO.getManagerId());
            staff.setStatus(0); // Initial status is pending review
            staff.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            staff.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            
            // Save application record
            boolean saved = save(staff);
            if (!saved) {
                return Result.error(500, "Failed to submit application");
            }
            
            return Result.success(staff, "Application submitted successfully, waiting for manager review");
        } catch (Exception e) {
            log.error("Failed to submit staff application: ", e);
            return Result.error(500, "Failed to submit staff application: " + e.getMessage());
        }
    }
}