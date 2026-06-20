package com.zinc.zinctalk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_chat_room")
public class ChatRoom {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerId;
    private String name;
    private Integer type;
    private LocalDateTime createTime;

    private Boolean isDeleted;

    @TableField(exist = false)
    private String peerName;
    @TableField(exist = false)
    private Long peerUserId;
}
