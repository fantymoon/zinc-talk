package com.zinc.zinctalk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("t_song")
public class Song {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long songlistId;    
    private String name;
    private String artist;
    private Integer duration;
    private String url;

    @TableLogic
    private Boolean isDeleted;
}
