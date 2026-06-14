package com.zinc.zinctalk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("t_chat_room_member")
public class ChatRoomMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private String role;
    private Long userId;

    @TableLogic
    private Boolean isDeleted;
 }
