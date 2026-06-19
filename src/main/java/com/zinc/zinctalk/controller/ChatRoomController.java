package com.zinc.zinctalk.controller;

import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.ChatRoom;
import com.zinc.zinctalk.service.ChatRoomService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @PostMapping("/room/private")
    public Result<ChatRoom> getOrCreatePrivateRoom(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long friendId = params.get("friendId");
        return chatRoomService.getOrCreatePrivateRoom(userId, friendId);
    }
}
