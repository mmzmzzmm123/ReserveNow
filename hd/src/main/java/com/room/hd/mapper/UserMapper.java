package com.room.hd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.room.hd.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * User Mapper Interface
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * Custom pagination query for user list
     * @param offset Offset
     * @param limit Page size
     * @param keyword Search keyword (username or email)
     * @param role Role filter
     * @param status Status filter
     * @return User list
     */
    @Select({"<script>",
            "SELECT id, name, email, password, role, avatar, created_at, updated_at, status FROM users",
            "<where>",
            "<if test='keyword != null and keyword != \"\"'>",
            "AND (name LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "<if test='role != null'>",
            "AND role = #{role}",
            "</if>",
            "<if test='status != null'>",
            "AND status = #{status}",
            "</if>",
            "</where>",
            "ORDER BY id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"})
    List<User> getUserList(@Param("offset") int offset, @Param("limit") int limit, 
                          @Param("keyword") String keyword, @Param("role") Integer role, 
                          @Param("status") Integer status);
    
    /**
     * Count total users matching the criteria
     * @param keyword Search keyword (username or email)
     * @param role Role filter
     * @param status Status filter
     * @return Total number of users
     */
    @Select({"<script>",
            "SELECT COUNT(*) FROM users",
            "<where>",
            "<if test='keyword != null and keyword != \"\"'>",
            "AND (name LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "<if test='role != null'>",
            "AND role = #{role}",
            "</if>",
            "<if test='status != null'>",
            "AND status = #{status}",
            "</if>",
            "</where>",
            "</script>"})
    long countUsers(@Param("keyword") String keyword, @Param("role") Integer role,
                   @Param("status") Integer status);
} 