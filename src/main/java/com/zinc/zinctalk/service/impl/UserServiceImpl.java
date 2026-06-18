package com.zinc.zinctalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.User;
import com.zinc.zinctalk.mapper.UserMapper;
import com.zinc.zinctalk.service.UserService;
import com.zinc.zinctalk.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    //注册
    @Override
    public Result<String> register(User user) {
        if (user.getAccount() == null || user.getAccount().isEmpty()) {
            return Result.fail("账号不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return Result.fail("密码不能为空");
        }
        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            return Result.fail("昵称不能为空");
        }
        if (!user.getAccount().matches("\\d{10}")) {
            return Result.fail("账号必须为10位数字");
        }
        if (user.getPassword().length() < 6) {
            return Result.fail("密码长度不能少于6位");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, user.getAccount());
        User exist = getOne(wrapper);

        if (exist != null) {
            return Result.fail("账号已存在");
        }
        if (user.getSex() == null || user.getSex().isEmpty()) {
            user.setSex("保密");
        }
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar("");
        }
        if (user.getSelfIntroduction() == null || user.getSelfIntroduction().isEmpty()) {
            user.setSelfIntroduction("");
        }
        save(user);
        return Result.success("注册成功");
    }
    
    //登录
    @Override
    public Result<Map<String, Object>> login(String account, String password) {
        if (account == null || account.isEmpty()) {
            return Result.fail("账号不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.fail("密码不能为空");
        }

        //mp的查询包装器
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();//创建查询
        wrapper.eq(User::getAccount, account);//条件
        User user = getOne(wrapper);//执行

        if (user == null) {
            return Result.fail("账号不存在");
        }
        if (!user.getPassword().equals(password)) {
            return Result.fail("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getAccount());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        return Result.success(data);
    }

    @Override
    public Result<User> getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

}
