package com.zinc.zinctalk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zinc.zinctalk.entity.ChatRoom;
import com.zinc.zinctalk.mapper.ChatRoomMapper;
import com.zinc.zinctalk.service.ChatRoomService;
import org.springframework.stereotype.Service;

@Service
public class ChatRoomServiceImpl extends ServiceImpl<ChatRoomMapper, ChatRoom> implements ChatRoomService {
}
