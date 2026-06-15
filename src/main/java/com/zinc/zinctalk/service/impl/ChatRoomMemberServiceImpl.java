package com.zinc.zinctalk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zinc.zinctalk.entity.ChatRoomMember;
import com.zinc.zinctalk.mapper.ChatRoomMemberMapper;
import com.zinc.zinctalk.service.ChatRoomMemberService;
import org.springframework.stereotype.Service;

@Service
public class ChatRoomMemberServiceImpl extends ServiceImpl<ChatRoomMemberMapper, ChatRoomMember> implements ChatRoomMemberService {
}
