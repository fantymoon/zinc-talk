package com.zinc.zinctalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.ChatRoom;
import com.zinc.zinctalk.entity.ChatRoomMember;
import com.zinc.zinctalk.entity.Friend;
import com.zinc.zinctalk.entity.User;
import com.zinc.zinctalk.mapper.ChatRoomMapper;
import com.zinc.zinctalk.mapper.ChatRoomMemberMapper;
import com.zinc.zinctalk.mapper.FriendMapper;
import com.zinc.zinctalk.mapper.UserMapper;
import com.zinc.zinctalk.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatRoomServiceImpl extends ServiceImpl<ChatRoomMapper, ChatRoom> implements ChatRoomService {

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private ChatRoomMemberMapper chatRoomMemberMapper;

    @Autowired
    private UserMapper userMapper;

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

    @Override
    @Transactional
    public Result<ChatRoom> createGroupRoom(Long userId, String name, List<Long> memberIds) {
        if (CollectionUtils.isEmpty(memberIds)) {
            return Result.fail("群聊成员不能为空");
        }

        Result<String> checkResult = checkAllAreFriends(userId, memberIds);
        if (checkResult != null) {
            return Result.fail(checkResult.getMessage());
        }

        ChatRoom room = new ChatRoom();
        room.setOwnerId(userId);
        room.setName(name);
        room.setType(2);
        save(room);

        ChatRoomMember owner = new ChatRoomMember();
        owner.setRoomId(room.getId());
        owner.setUserId(userId);
        owner.setRole("1");  // 1 = 群主
        chatRoomMemberMapper.insert(owner);

        for (Long memberId : memberIds) {
            ChatRoomMember member = new ChatRoomMember();
            member.setRoomId(room.getId());
            member.setUserId(memberId);
            member.setRole("0");  // 0 = 普通成员
            chatRoomMemberMapper.insert(member);
        }

        return Result.success(room);
    }

    //邀请
    @Override
    @Transactional
    public Result<String> inviteMembers(Long userId, Long roomId, List<Long> memberIds) {
        if (CollectionUtils.isEmpty(memberIds)) {
            return Result.fail("邀请成员不能为空");
        }

        ChatRoom room = getById(roomId);
        if (room == null || Boolean.TRUE.equals(room.getIsDeleted())) {
            return Result.fail("群聊不存在");
        }
        if (!room.getOwnerId().equals(userId)) {
            return Result.fail("只有群主可以邀请成员");
        }

        Result<String> checkResult = checkAllAreFriends(userId, memberIds);
        if (checkResult != null) {
            return Result.fail(checkResult.getMessage());
        }

        LambdaQueryWrapper<ChatRoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatRoomMember::getRoomId, roomId)
            .in(ChatRoomMember::getUserId, memberIds)
            .eq(ChatRoomMember::getIsDeleted, 0);
        List<ChatRoomMember> existingMembers = chatRoomMemberMapper.selectList(wrapper);
        if (!existingMembers.isEmpty()) {
            return Result.fail("部分用户已在群中");
        }

        for (Long memberId : memberIds) {
            ChatRoomMember member = new ChatRoomMember();
            member.setRoomId(roomId);
            member.setUserId(memberId);
            member.setRole("0");  // 0 = 普通成员
            chatRoomMemberMapper.insert(member);
        }

        return Result.success("邀请成功");
    }

    //退出聊天室
    @Override
    @Transactional
    public Result<String> leaveGroup(Long userId, Long roomId) {
        ChatRoom room = getById(roomId);
        if (room == null || Boolean.TRUE.equals(room.getIsDeleted())) {
            return Result.fail("群聊不存在");
        }
        if (room.getOwnerId().equals(userId)) {
            return Result.fail("群主不能退群，请解散群聊");
        }

        LambdaUpdateWrapper<ChatRoomMember> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatRoomMember::getRoomId, roomId)
            .eq(ChatRoomMember::getUserId, userId)
            .eq(ChatRoomMember::getIsDeleted, 0);
        int rows = chatRoomMemberMapper.delete(wrapper);
        if (rows == 0) {
            return Result.fail("您不在该群聊中");
        }

        return Result.success("已退出群聊");
    }

    //解散聊天室
    @Override
    @Transactional
    public Result<String> dissolveGroup(Long userId, Long roomId) {
        ChatRoom room = getById(roomId);
        if (room == null || Boolean.TRUE.equals(room.getIsDeleted())) {
            return Result.fail("群聊不存在");
        }
        if (!room.getOwnerId().equals(userId)) {
            return Result.fail("只有群主可以解散群聊");
        }

        removeById(roomId);

        LambdaUpdateWrapper<ChatRoomMember> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatRoomMember::getRoomId, roomId)
            .eq(ChatRoomMember::getIsDeleted, 0);
        chatRoomMemberMapper.delete(wrapper);

        return Result.success("群聊已解散");
    }

    //更新聊天室信息
    @Override
    public Result<ChatRoom> updateGroupRoom(Long userId, Long roomId, String name) {
        ChatRoom room = getById(roomId);
        if (room == null || Boolean.TRUE.equals(room.getIsDeleted())) {
            return Result.fail("群聊不存在");
        }
        if (!room.getOwnerId().equals(userId)) {
            return Result.fail("只有群主可以修改群聊信息");
        }
        if (room.getType() != 2) {
            return Result.fail("只有群聊可以修改信息");
        }
        if (name == null || name.trim().isEmpty()) {
            return Result.fail("群聊名称不能为空");
        }

        room.setName(name.trim());
        updateById(room);
        return Result.success(room);
    }

    //获取聊天室成员
    @Override
    public Result<List<ChatRoomMember>> getRoomMembers(Long userId, Long roomId) {
        if (!isRoomMember(roomId, userId)) {
            return Result.fail("您不在该聊天室中");
        }

        LambdaQueryWrapper<ChatRoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatRoomMember::getRoomId, roomId)
            .eq(ChatRoomMember::getIsDeleted, 0);
        List<ChatRoomMember> members = chatRoomMemberMapper.selectList(wrapper);

        fillMemberNicknames(members);

        return Result.success(members);
    }

    //获取用户所有聊天室
    @Override
    public Result<List<ChatRoom>> getMyRooms(Long userId) {
        List<Long> roomIds = baseMapper.selectRoomIdsByUserId(userId);
        if (roomIds.isEmpty()) {
            return Result.success(List.of());
        }

        LambdaQueryWrapper<ChatRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ChatRoom::getId, roomIds)
            .eq(ChatRoom::getIsDeleted, 0)
            .orderByDesc(ChatRoom::getCreateTime);
        List<ChatRoom> rooms = list(wrapper);

        fillPrivatePeerNames(rooms, userId);

        return Result.success(rooms);
    }

    //给私聊房间批量填充对方昵称
    private void fillPrivatePeerNames(List<ChatRoom> rooms, Long userId) {
        List<Long> privateRoomIds = rooms.stream()
            .filter(r -> r.getType() != null && r.getType() == 1)
            .map(ChatRoom::getId)
            .collect(Collectors.toList());
        if (privateRoomIds.isEmpty()) return;

        LambdaQueryWrapper<ChatRoomMember> mw = new LambdaQueryWrapper<>();
        mw.in(ChatRoomMember::getRoomId, privateRoomIds)
            .eq(ChatRoomMember::getIsDeleted, 0);
        List<ChatRoomMember> members = chatRoomMemberMapper.selectList(mw);

        Map<Long, Long> roomPeerUserId = new HashMap<>();
        for (ChatRoomMember m : members) {
            if (!userId.equals(m.getUserId())) {
                roomPeerUserId.put(m.getRoomId(), m.getUserId());
            }
        }
        if (roomPeerUserId.isEmpty()) return;

        List<Long> peerIds = new ArrayList<>(new HashSet<>(roomPeerUserId.values()));
        Map<Long, String> nameMap = userMapper.selectBatchIds(peerIds).stream()
            .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));

        for (ChatRoom room : rooms) {
            Long peerId = roomPeerUserId.get(room.getId());
            if (peerId != null) {
                room.setPeerUserId(peerId);
                room.setPeerName(nameMap.getOrDefault(peerId, "未知用户"));
            }
        }
    }

    //给成员列表批量填充昵称
    private void fillMemberNicknames(List<ChatRoomMember> members) {
        if (members == null || members.isEmpty()) return;
        List<Long> ids = members.stream()
            .map(ChatRoomMember::getUserId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, String> nameMap = userMapper.selectBatchIds(ids).stream()
            .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));
        members.forEach(m -> m.setNickname(nameMap.get(m.getUserId())));
    }

    //检查所有成员是否为好友
    private Result<String> checkAllAreFriends(Long userId, List<Long> memberIds) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
            .in(Friend::getFriendId, memberIds)
            .eq(Friend::getStatus, 1)
            .eq(Friend::getIsDeleted, 0);
        List<Friend> friends = friendMapper.selectList(wrapper);

        List<Long> friendIds = friends.stream()
            .map(Friend::getFriendId)
            .collect(Collectors.toList());

        for (Long memberId : memberIds) {
            if (!friendIds.contains(memberId)) {
                return Result.fail("用户 " + memberId + " 不是您的好友");
            }
        }
        return null;
    }

    //检查是否为聊天室成员
    private boolean isRoomMember(Long roomId, Long userId) {
        LambdaQueryWrapper<ChatRoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatRoomMember::getRoomId, roomId)
            .eq(ChatRoomMember::getUserId, userId)
            .eq(ChatRoomMember::getIsDeleted, 0);
        return chatRoomMemberMapper.selectCount(wrapper) > 0;
    }
}
