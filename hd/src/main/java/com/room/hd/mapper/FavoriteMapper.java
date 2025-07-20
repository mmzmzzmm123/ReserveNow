package com.room.hd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.room.hd.entity.Favorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Favorite Mapper Interface
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
    
    /**
     * Check if already favorited
     * @param userId User ID
     * @param restaurantId Restaurant ID
     * @return Number of favorite records
     */
    @Select("SELECT COUNT(*) FROM favorites WHERE user_id = #{userId} AND restaurant_id = #{restaurantId}")
    int checkFavorite(@Param("userId") Integer userId, @Param("restaurantId") Integer restaurantId);
} 