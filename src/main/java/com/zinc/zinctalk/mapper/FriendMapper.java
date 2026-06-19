package com.zinc.zinctalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zinc.zinctalk.entity.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FriendMapper extends BaseMapper<Friend> {

    @Select("SELECT * FROM t_friend WHERE user_id = #{userId} AND friend_id = #{friendId} AND is_deleted = 1")
    Friend selectDeleted(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Update("UPDATE t_friend SET status = #{status}, is_deleted = 0, update_time = NOW() WHERE user_id = #{userId} AND friend_id = #{friendId}")
    int restore(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("status") Integer status);
}
