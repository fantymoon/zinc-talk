package com.zinc.zinctalk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("t_songlist")
public class Songlist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerId;
    private String name;

    @TableLogic
    private Boolean isDeleted;
}
