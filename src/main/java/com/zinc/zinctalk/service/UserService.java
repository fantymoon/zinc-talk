package com.zinc.zinctalk.service;

import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.User;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {

    Result<String> register(User user);

    Result<Map<String, Object>> login(String account, String password);

    Result<User> getUserInfo(Long userId);

    Result<User> updateProfile(Long userId, User user);

    Result<String> updatePassword(Long userId, String oldPassword, String newPassword);

}
