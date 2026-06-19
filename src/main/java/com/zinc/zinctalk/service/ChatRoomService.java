package com.zinc.zinctalk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.ChatRoom;

public interface ChatRoomService extends IService<ChatRoom> {
    Result<ChatRoom> getOrCreatePrivateRoom(Long userId, Long friendId);
}
