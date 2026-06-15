package com.zinc.zinctalk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zinc.zinctalk.entity.Friend;
import com.zinc.zinctalk.mapper.FriendMapper;
import com.zinc.zinctalk.service.FriendService;
import org.springframework.stereotype.Service;

@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {
}
