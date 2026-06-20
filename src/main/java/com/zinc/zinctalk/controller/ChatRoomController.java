package com.zinc.zinctalk.controller;

import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.ChatRoom;
import com.zinc.zinctalk.entity.ChatRoomMember;
import com.zinc.zinctalk.service.ChatRoomService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @PostMapping("/room/group")
    public Result<ChatRoom> createGroupRoom(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String name = (String) params.get("name");
        List<?> rawList = (List<?>) params.get("memberIds");
        List<Long> memberIds = rawList.stream()
            .map(o -> Long.valueOf(o.toString()))
            .collect(Collectors.toList());
        return chatRoomService.createGroupRoom(userId, name, memberIds);
    }

    @PostMapping("/room/{roomId}/invite")
    public Result<String> inviteMembers(@PathVariable Long roomId,
                                        @RequestBody Map<String, Object> params,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<?> rawList = (List<?>) params.get("memberIds");
        List<Long> memberIds = rawList.stream()
            .map(o -> Long.valueOf(o.toString()))
            .collect(Collectors.toList());
        return chatRoomService.inviteMembers(userId, roomId, memberIds);
    }

    @PostMapping("/room/{roomId}/leave")
    public Result<String> leaveGroup(@PathVariable Long roomId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return chatRoomService.leaveGroup(userId, roomId);
    }

    @DeleteMapping("/room/{roomId}")
    public Result<String> dissolveGroup(@PathVariable Long roomId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return chatRoomService.dissolveGroup(userId, roomId);
    }

    @GetMapping("/room/{roomId}/members")
    public Result<List<ChatRoomMember>> getRoomMembers(@PathVariable Long roomId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return chatRoomService.getRoomMembers(userId, roomId);
    }

    @PutMapping("/room/{roomId}")
    public Result<ChatRoom> updateGroupRoom(@PathVariable Long roomId,
                                            @RequestBody Map<String, Object> params,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String name = (String) params.get("name");
        return chatRoomService.updateGroupRoom(userId, roomId, name);
    }

    @GetMapping("/room/list")
    public Result<List<ChatRoom>> getMyRooms(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return chatRoomService.getMyRooms(userId);
    }
}
