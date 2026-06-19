package com.zinc.zinctalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zinc.zinctalk.entity.ChatRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatRoomMapper extends BaseMapper<ChatRoom> {

    @Select("SELECT r.id FROM t_chat_room r " +
            "JOIN t_chat_room_member m1 ON r.id = m1.room_id AND m1.user_id = #{userId1} AND m1.is_deleted = 0 " +
            "JOIN t_chat_room_member m2 ON r.id = m2.room_id AND m2.user_id = #{userId2} AND m2.is_deleted = 0 " +
            "WHERE r.type = 1 AND r.is_deleted = 0 " +
            "LIMIT 1")
    Long selectPrivateRoomId(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Select("SELECT DISTINCT room_id FROM t_chat_room_member " +
            "WHERE user_id = #{userId} AND is_deleted = 0")
    List<Long> selectRoomIdsByUserId(@Param("userId") Long userId);
}
