package com.zinc.zinctalk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zinc.zinctalk.entity.Friend;

import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.User;
import java.util.List;
import java.util.Map;

public interface FriendService extends IService<Friend> {

    Result<List<User>> getFriendList(Long userId);
    Result<String> addFriend(Long userId, Long friendId);
    Result<List<Map<String, Object>>> getFriendRequests(Long userId);
    Result<String> acceptFriend(Long userId, Long friendId);
    Result<String> rejectFriend(Long userId, Long friendId);
    Result<String> deleteFriend(Long userId, Long friendId);
}
