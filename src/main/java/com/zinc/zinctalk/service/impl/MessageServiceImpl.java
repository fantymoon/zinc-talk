package com.zinc.zinctalk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zinc.zinctalk.entity.Message;
import com.zinc.zinctalk.mapper.MessageMapper;
import com.zinc.zinctalk.service.MessageService;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
}
