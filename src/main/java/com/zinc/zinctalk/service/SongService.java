package com.zinc.zinctalk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.Song;

import java.util.List;

public interface SongService extends IService<Song> {
    Result<Song> uploadSong(Long userId, Long songlistId, String name,
                            String artist, Integer duration, String url);
    Result<Song> getSongDetail(Long songId);
    Result<String> deleteSong(Long userId, Long songId);
    Result<List<Song>> getSonglistSongs(Long userId, Long songlistId);
}
