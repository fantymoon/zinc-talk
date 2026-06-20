package com.zinc.zinctalk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.Songlist;

import java.util.List;

public interface SonglistService extends IService<Songlist> {
    Result<Songlist> createSonglist(Long userId, String name);
    Result<List<Songlist>> getMySonglists(Long userId);
    Result<String> updateSonglist(Long userId, Long songlistId, String name);
    Result<String> deleteSonglist(Long userId, Long songlistId);
}
