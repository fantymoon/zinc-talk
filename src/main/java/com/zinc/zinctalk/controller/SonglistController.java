package com.zinc.zinctalk.controller;

import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.Songlist;
import com.zinc.zinctalk.service.SonglistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/songlist")
public class SonglistController {

    @Autowired
    private SonglistService songlistService;

    //创建歌单
    @PostMapping
    public Result<Songlist> createSonglist(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String name = params.get("name");
        return songlistService.createSonglist(userId, name);
    }

    //获取我的歌单列表
    @GetMapping("/mine")
    public Result<List<Songlist>> getMySonglists(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return songlistService.getMySonglists(userId);
    }

    //修改歌单名称
    @PutMapping("/{songlistId}")
    public Result<String> updateSonglist(@PathVariable Long songlistId,
                                         @RequestBody Map<String, String> params,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String name = params.get("name");
        return songlistService.updateSonglist(userId, songlistId, name);
    }

    //删除歌单
    @DeleteMapping("/{songlistId}")
    public Result<String> deleteSonglist(@PathVariable Long songlistId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return songlistService.deleteSonglist(userId, songlistId);
    }
}
