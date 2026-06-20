package com.zinc.zinctalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.ChatRoomMember;
import com.zinc.zinctalk.entity.Message;
import com.zinc.zinctalk.entity.User;
import com.zinc.zinctalk.mapper.ChatRoomMemberMapper;
import com.zinc.zinctalk.mapper.MessageMapper;
import com.zinc.zinctalk.mapper.UserMapper;
import com.zinc.zinctalk.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    private ChatRoomMemberMapper chatRoomMemberMapper;

    @Autowired
    private UserMapper userMapper;

    //保存消息
    @Override
    public Result<Message> saveMessage(Long senderId, Long roomId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return Result.fail("消息内容不能为空");
        }

        if (!isRoomMember(roomId, senderId)) {
            return Result.fail("不在该聊天室中");
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setRoomId(roomId);
        message.setContent(content.trim());
        message.setSendTime(LocalDateTime.now());
        save(message);

        return Result.success(message);
    }

    //获取历史消息
    @Override
    public Result<List<Message>> getHistoryMessages(Long roomId, Long userId) {
        if (!isRoomMember(roomId, userId)) {
            return Result.fail("不在该聊天室中");
        }

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getRoomId, roomId)
            .orderByAsc(Message::getSendTime);
        List<Message> messages = list(wrapper);

        //批量填充发送者昵称
        if (!messages.isEmpty()) {
            List<Long> senderIds = messages.stream()
                .map(Message::getSenderId)
                .distinct()
                .collect(Collectors.toList());
            Map<Long, String> nameMap = userMapper.selectBatchIds(senderIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));
            messages.forEach(m -> m.setSenderName(nameMap.get(m.getSenderId())));
        }

        return Result.success(messages);
    }

    //判断用户是否在聊天室中
    private boolean isRoomMember(Long roomId, Long userId) {
        LambdaQueryWrapper<ChatRoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatRoomMember::getRoomId, roomId)
            .eq(ChatRoomMember::getUserId, userId);
        return chatRoomMemberMapper.selectCount(wrapper) > 0;
    }
}
