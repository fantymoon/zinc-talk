package com.zinc.zinctalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.ChatRoom;
import com.zinc.zinctalk.entity.ChatRoomMember;
import com.zinc.zinctalk.entity.Friend;
import com.zinc.zinctalk.mapper.ChatRoomMapper;
import com.zinc.zinctalk.mapper.ChatRoomMemberMapper;
import com.zinc.zinctalk.mapper.FriendMapper;
import com.zinc.zinctalk.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatRoomServiceImpl extends ServiceImpl<ChatRoomMapper, ChatRoom> implements ChatRoomService {

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private ChatRoomMemberMapper chatRoomMemberMapper;

    @Override
    @Transactional
    public Result<ChatRoom> getOrCreatePrivateRoom(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            return Result.fail("不能和自己聊天");
        }

        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
            .eq(Friend::getFriendId, friendId)
            .eq(Friend::getStatus, 1);
        Friend friend = friendMapper.selectOne(wrapper);
        if (friend == null) {
            return Result.fail("对方不是您的好友");
        }

        Long roomId = baseMapper.selectPrivateRoomId(userId, friendId);
        if (roomId != null) {
            ChatRoom room = getById(roomId);
            return Result.success(room);
        }

        ChatRoom room = new ChatRoom();
        room.setOwnerId(userId);
        room.setName("");
        room.setType(1);
        save(room);

        ChatRoomMember member1 = new ChatRoomMember();
        member1.setRoomId(room.getId());
        member1.setUserId(userId);
        member1.setRole("0");
        chatRoomMemberMapper.insert(member1);

        ChatRoomMember member2 = new ChatRoomMember();
        member2.setRoomId(room.getId());
        member2.setUserId(friendId);
        member2.setRole("0");
        chatRoomMemberMapper.insert(member2);

        return Result.success(room);
    }
}
