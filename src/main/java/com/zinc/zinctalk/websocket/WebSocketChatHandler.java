package com.zinc.zinctalk.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.ChatRoomMember;
import com.zinc.zinctalk.entity.Friend;
import com.zinc.zinctalk.entity.Message;
import com.zinc.zinctalk.entity.Song;
import com.zinc.zinctalk.entity.User;
import com.zinc.zinctalk.mapper.ChatRoomMemberMapper;
import com.zinc.zinctalk.mapper.FriendMapper;
import com.zinc.zinctalk.mapper.SongMapper;
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

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private SongMapper songMapper;

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
            } else if ("share-friend".equals(type)) {
                Long roomId = Long.valueOf(req.get("roomId").toString());
                Long friendUserId = Long.valueOf(req.get("friendUserId").toString());
                handleShareFriend(userId, account, roomId, friendUserId);
            } else if ("share-music".equals(type)) {
                Long roomId = Long.valueOf(req.get("roomId").toString());
                Long songId = Long.valueOf(req.get("songId").toString());
                handleShareMusic(userId, account, roomId, songId);
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

    //好友推荐
    private void handleShareFriend(Long senderId, String account, Long roomId, Long friendUserId) throws IOException {
        if (friendUserId.equals(senderId)) {
            WebSocketSession session = sessions.get(senderId);
            if (session != null) sendToSession(session, Result.fail("不能推荐自己"));
            return;
        }

        //校验被推荐人是发送者的好友
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Friend> fw =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        fw.eq(Friend::getUserId, senderId)
            .eq(Friend::getFriendId, friendUserId)
            .eq(Friend::getStatus, 1);
        Friend friend = friendMapper.selectOne(fw);
        if (friend == null) {
            WebSocketSession session = sessions.get(senderId);
            if (session != null) sendToSession(session, Result.fail("只能推荐自己的好友"));
            return;
        }

        User recommended = userMapper.selectById(friendUserId);
        if (recommended == null) {
            WebSocketSession session = sessions.get(senderId);
            if (session != null) sendToSession(session, Result.fail("被推荐用户不存在"));
            return;
        }

        Result<Message> result = messageService.saveShareMessage(senderId, roomId, 1, String.valueOf(friendUserId));
        if (!result.getCode().equals(200)) {
            WebSocketSession session = sessions.get(senderId);
            if (session != null) sendToSession(session, result);
            return;
        }

        Message message = result.getData();
        User sender = userMapper.selectById(senderId);
        String senderName = sender != null ? sender.getNickname() : account;

        Map<String, Object> resp = new HashMap<>();
        resp.put("type", "share-friend");
        resp.put("messageId", message.getId());
        resp.put("roomId", roomId);
        resp.put("senderId", senderId);
        resp.put("senderName", senderName);
        resp.put("friendUserId", friendUserId);
        resp.put("friendNickname", recommended.getNickname());
        resp.put("friendAvatar", recommended.getAvatar());
        resp.put("friendAccount", recommended.getAccount());
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

    //音乐分享
    private void handleShareMusic(Long senderId, String account, Long roomId, Long songId) throws IOException {
        Song song = songMapper.selectById(songId);
        if (song == null) {
            WebSocketSession session = sessions.get(senderId);
            if (session != null) sendToSession(session, Result.fail("歌曲不存在"));
            return;
        }

        //存一份歌曲快照进extra 发送者删歌后好友仍可试听或保存
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("songId", song.getId());
        snapshot.put("name", song.getName());
        snapshot.put("artist", song.getArtist());
        snapshot.put("url", song.getUrl());
        snapshot.put("duration", song.getDuration());
        String extraJson = objectMapper.writeValueAsString(snapshot);

        Result<Message> result = messageService.saveShareMessage(senderId, roomId, 2, extraJson);
        if (!result.getCode().equals(200)) {
            WebSocketSession session = sessions.get(senderId);
            if (session != null) sendToSession(session, result);
            return;
        }

        Message message = result.getData();
        User sender = userMapper.selectById(senderId);
        String senderName = sender != null ? sender.getNickname() : account;

        Map<String, Object> resp = new HashMap<>();
        resp.put("type", "share-music");
        resp.put("messageId", message.getId());
        resp.put("roomId", roomId);
        resp.put("senderId", senderId);
        resp.put("senderName", senderName);
        resp.put("songId", song.getId());
        resp.put("songName", song.getName());
        resp.put("songArtist", song.getArtist());
        resp.put("songUrl", song.getUrl());
        resp.put("songDuration", song.getDuration());
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
