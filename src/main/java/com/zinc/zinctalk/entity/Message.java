package com.zinc.zinctalk.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("t_message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private String content;
    private Integer type;       //0文本 1好友推荐 2音乐分享
    private String extra;       //好友推荐时存被推荐人userId
    private LocalDateTime sendTime;
    private Long roomId;

    @TableField(exist = false)
    private String senderName;

    //好友推荐卡片的展示信息
    @TableField(exist = false)
    private Long friendUserId;
    @TableField(exist = false)
    private String friendNickname;
    @TableField(exist = false)
    private String friendAvatar;
    @TableField(exist = false)
    private String friendAccount;

    //音乐分享卡片的展示信息
    @TableField(exist = false)
    private Long songId;
    @TableField(exist = false)
    private String songName;
    @TableField(exist = false)
    private String songArtist;
    @TableField(exist = false)
    private String songUrl;
    @TableField(exist = false)
    private Integer songDuration;
}
