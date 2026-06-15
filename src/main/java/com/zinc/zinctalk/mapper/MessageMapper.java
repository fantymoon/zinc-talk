package com.zinc.zinctalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zinc.zinctalk.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}
