package com.zinc.zinctalk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.ChatRoom;
import com.zinc.zinctalk.entity.ChatRoomMember;

import java.util.List;

public interface ChatRoomService extends IService<ChatRoom> {
    Result<ChatRoom> getOrCreatePrivateRoom(Long userId, Long friendId);

    Result<ChatRoom> createGroupRoom(Long userId, String name, List<Long> memberIds);

    Result<String> inviteMembers(Long userId, Long roomId, List<Long> memberIds);

    Result<String> leaveGroup(Long userId, Long roomId);

    Result<String> dissolveGroup(Long userId, Long roomId);

    Result<List<ChatRoomMember>> getRoomMembers(Long userId, Long roomId);

    Result<List<ChatRoom>> getMyRooms(Long userId);

    Result<ChatRoom> updateGroupRoom(Long userId, Long roomId, String name);
}
