package com.zinc.zinctalk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("t_friend")
public class Friend {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;    
    private Long friendId;

    @TableLogic
    private Boolean isDeleted;
 }
