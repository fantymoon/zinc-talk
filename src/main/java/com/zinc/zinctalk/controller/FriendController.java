package com.zinc.zinctalk.controller;

import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.User;
import com.zinc.zinctalk.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @GetMapping("/list")
    public Result<List<User>> getFriendList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return friendService.getFriendList(userId);
    }
    
    @PostMapping("/request")
    public Result<String> addFriend(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long friendId = params.get("friendId");
        return friendService.addFriend(userId, friendId);
    }

    @GetMapping("/requests")
    public Result<List<Map<String, Object>>> getFriendRequests(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return friendService.getFriendRequests(userId);
    }

    @PostMapping("/accept")
    public Result<String> acceptFriend(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long friendId = params.get("friendId");
        return friendService.acceptFriend(userId, friendId);
    }

    @PostMapping("/reject")
    public Result<String> rejectFriend(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long friendId = params.get("friendId");
        return friendService.rejectFriend(userId, friendId);
    }

    @DeleteMapping("/{friendId}")
    public Result<String> deleteFriend(@PathVariable Long friendId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return friendService.deleteFriend(userId, friendId);
    }
}