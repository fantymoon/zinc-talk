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

    //保存分享消息
    @Override
    public Result<Message> saveShareMessage(Long senderId, Long roomId, Integer type, String extra) {
        if (type == null) {
            return Result.fail("消息类型不能为空");
        }
        if (!isRoomMember(roomId, senderId)) {
            return Result.fail("不在该聊天室中");
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setRoomId(roomId);
        message.setType(type);
        message.setExtra(extra);
        message.setContent(type == 1 ? "[好友推荐]" : "[分享]");
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

        //批量填充发送者昵称+好友推荐卡片信息
        if (!messages.isEmpty()) {
            List<Long> senderIds = messages.stream()
                .map(Message::getSenderId)
                .distinct()
                .collect(Collectors.toList());
            Map<Long, String> nameMap = userMapper.selectBatchIds(senderIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));
            Map<Long, User> friendMap = friendMapOf(messages);
            messages.forEach(m -> {
                m.setSenderName(nameMap.get(m.getSenderId()));
                if (m.getType() != null && m.getType() == 1 && m.getExtra() != null) {
                    try {
                        Long fid = Long.valueOf(m.getExtra());
                        User u = friendMap.get(fid);
                        if (u != null) {
                            m.setFriendUserId(fid);
                            m.setFriendNickname(u.getNickname());
                            m.setFriendAvatar(u.getAvatar());
                            m.setFriendAccount(u.getAccount());
                        }
                    } catch (NumberFormatException ignored) { }
                }
            });
        }

        return Result.success(messages);
    }

    //收集好友推荐消息中extra对应的user
    private Map<Long, User> friendMapOf(List<Message> messages) {
        List<Long> friendUserIds = messages.stream()
            .filter(m -> m.getType() != null && m.getType() == 1 && m.getExtra() != null)
            .map(m -> Long.valueOf(m.getExtra()))
            .distinct()
            .collect(Collectors.toList());
        if (friendUserIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(friendUserIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
    }

    //判断用户是否在聊天室中
    private boolean isRoomMember(Long roomId, Long userId) {
        LambdaQueryWrapper<ChatRoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatRoomMember::getRoomId, roomId)
            .eq(ChatRoomMember::getUserId, userId);
        return chatRoomMemberMapper.selectCount(wrapper) > 0;
    }
}
