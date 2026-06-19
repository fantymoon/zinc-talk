package com.zinc.zinctalk.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.ChatRoomMember;
import com.zinc.zinctalk.entity.Message;
import com.zinc.zinctalk.entity.User;
import com.zinc.zinctalk.mapper.ChatRoomMemberMapper;
import com.zinc.zinctalk.mapper.UserMapper;
import com.zinc.zinctalk.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketChatHandler extends TextWebSocketHandler {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatRoomMemberMapper chatRoomMemberMapper;

    @Autowired
    private UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            session.close();
            return;
        }
        sessions.put(userId, session);
        System.out.println("[WebSocket] 用户上线: " + userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        String account = (String) session.getAttributes().get("account");
        if (userId == null) {
            return;
        }

        try {
            Map<String, Object> req = objectMapper.readValue(textMessage.getPayload(), Map.class);
            String type = (String) req.get("type");

            if ("message".equals(type)) {
                Long roomId = Long.valueOf(req.get("roomId").toString());
                String content = (String) req.get("content");
                handleChatMessage(userId, account, roomId, content);
            }
        } catch (Exception e) {
            System.out.println("[WebSocket] 消息处理异常: " + e.getMessage());
            sendToSession(session, Result.fail("消息格式错误"));
        }
    }

    private void handleChatMessage(Long senderId, String account, Long roomId, String content) throws IOException {
        Result<Message> result = messageService.saveMessage(senderId, roomId, content);
        if (!result.getCode().equals(200)) {
            WebSocketSession session = sessions.get(senderId);
            if (session != null) {
                sendToSession(session, result);
            }
            return;
        }

        Message message = result.getData();
        User user = userMapper.selectById(senderId);
        String senderName = user != null ? user.getNickname() : account;

        Map<String, Object> resp = new HashMap<>();
        resp.put("type", "message");
        resp.put("messageId", message.getId());
        resp.put("roomId", roomId);
        resp.put("senderId", senderId);
        resp.put("senderName", senderName);
        resp.put("content", message.getContent());
        resp.put("sendTime", message.getSendTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        String respJson = objectMapper.writeValueAsString(resp);

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatRoomMember> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(ChatRoomMember::getRoomId, roomId);
        List<ChatRoomMember> members = chatRoomMemberMapper.selectList(wrapper);

        for (ChatRoomMember member : members) {
            WebSocketSession memberSession = sessions.get(member.getUserId());
            if (memberSession != null && memberSession.isOpen()) {
                memberSession.sendMessage(new TextMessage(respJson));
            }
        }
    }

    private void sendToSession(WebSocketSession session, Result<?> result) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(result)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(userId);
            System.out.println("[WebSocket] 用户下线: " + userId);
        }
    }
}
