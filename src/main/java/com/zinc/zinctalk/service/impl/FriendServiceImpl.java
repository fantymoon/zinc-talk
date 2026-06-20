package com.zinc.zinctalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.Friend;
import com.zinc.zinctalk.entity.User;
import com.zinc.zinctalk.mapper.FriendMapper;
import com.zinc.zinctalk.mapper.UserMapper;
import com.zinc.zinctalk.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FriendMapper friendMapper;


    //获取好友列表
    @Override
    public Result<List<User>> getFriendList(Long userId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
               .eq(Friend::getStatus, 1);
        List<Friend> friends = list(wrapper);

        List<User> userList = new ArrayList<>();
        for (Friend friend : friends) {
            User user = userMapper.selectById(friend.getFriendId());
            if (user != null) {
                user.setPassword(null);
                userList.add(user);
            }
        }
        return Result.success(userList);
    }

    //添加好友
    @Override
    public Result<String> addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            return Result.fail("不能添加自己为好友");
        }

        //校验目标用户是否存在
        if (userMapper.selectById(friendId) == null) {
            return Result.fail("用户不存在");
        }

        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
            .eq(Friend::getFriendId, friendId);
        Friend exist = getOne(wrapper);
        if (exist != null) {
            if (exist.getStatus() == 1) {
                return Result.fail("已经是好友");
            }
            if (exist.getStatus() == 0) {
                return Result.fail("好友申请已发送");
            }
            exist.setStatus(0);
            updateById(exist);
            return Result.success("好友申请已发送");
        }

        Friend deleted = friendMapper.selectDeleted(userId, friendId);
        if (deleted != null) {
            friendMapper.restore(userId, friendId, 0);
            return Result.success("好友申请已发送");
        }

        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setStatus(0);
        save(friend);
        return Result.success("好友申请已发送");
    }

    //好友申请列表
    @Override
    public Result<List<Map<String, Object>>> getFriendRequests(Long userId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getStatus, 0)
            .and(w -> w.eq(Friend::getUserId, userId).or().eq(Friend::getFriendId, userId));
        List<Friend> requests = list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Friend req : requests) {
            boolean isOutgoing = req.getUserId().equals(userId);
            Long otherId = isOutgoing ? req.getFriendId() : req.getUserId();
            User user = userMapper.selectById(otherId);
            if (user == null) {
                continue;
            }
            user.setPassword(null);

            Map<String, Object> item = new HashMap<>();
            item.put("user", user);
            item.put("direction", isOutgoing ? "outgoing" : "incoming");
            item.put("requestTime", req.getCreateTime());
            result.add(item);
        }
        return Result.success(result);
    }

    //同意申请
    @Override
    @Transactional
    public Result<String> acceptFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, friendId)
            .eq(Friend::getFriendId, userId)
            .eq(Friend::getStatus, 0);
        Friend request = getOne(wrapper);
        if (request == null) {
            return Result.fail("没有待处理的好友申请");
        }

        request.setStatus(1);
        updateById(request);

        Friend deleted = friendMapper.selectDeleted(userId, friendId);
        if (deleted != null) {
            friendMapper.restore(userId, friendId, 1);
        } else {
            LambdaQueryWrapper<Friend> reverseWrapper = new LambdaQueryWrapper<>();
            reverseWrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);
            Friend reverse = getOne(reverseWrapper);
            if (reverse == null) {
                reverse = new Friend();
                reverse.setUserId(userId);
                reverse.setFriendId(friendId);
                reverse.setStatus(1);
                save(reverse);
            } else {
                reverse.setStatus(1);
                updateById(reverse);
            }
        }
        return Result.success("已同意好友申请");
    }

    //拒绝
    @Override
    public Result<String> rejectFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, friendId)
            .eq(Friend::getFriendId, userId)
            .eq(Friend::getStatus, 0);
        Friend request = getOne(wrapper);
        if (request == null) {
            return Result.fail("没有待处理的好友申请");
        }

        request.setStatus(2);
        updateById(request);
        return Result.success("已拒绝好友申请");
    }

    //删除好友
    @Override
    @Transactional
    public Result<String> deleteFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(Friend::getUserId, userId)
            .eq(Friend::getFriendId, friendId);
        Friend f1 = getOne(wrapper1);

        LambdaQueryWrapper<Friend> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(Friend::getUserId, friendId)
            .eq(Friend::getFriendId, userId);
        Friend f2 = getOne(wrapper2);

        if (f1 == null && f2 == null) {
            return Result.fail("好友关系不存在");
        }

        if (f1 != null) {
            removeById(f1.getId());
        }
        if (f2 != null) {
            removeById(f2.getId());
        }
        return Result.success("已删除好友");
    }
}
