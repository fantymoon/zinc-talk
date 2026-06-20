package com.zinc.zinctalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.Song;
import com.zinc.zinctalk.entity.Songlist;
import com.zinc.zinctalk.mapper.SongMapper;
import com.zinc.zinctalk.mapper.SonglistMapper;
import com.zinc.zinctalk.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements SongService {

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private SonglistMapper songlistMapper;

    @Override
    public Result<Song> uploadSong(Long userId, Long songlistId, String name,
                                   String artist, Integer duration, String url) {

        Songlist songlist = songlistMapper.selectById(songlistId);
        if (songlist == null) {
            return Result.fail("歌单不存在");
        }
        if (!songlist.getOwnerId().equals(userId)) {
            return Result.fail("不能往别人的歌单添加歌曲");
        }

        if (name == null || name.trim().isEmpty()) {
            return Result.fail("歌曲名不能为空");
        }
        if (url == null || url.trim().isEmpty()) {
            return Result.fail("歌曲文件路径不能为空");
        }

        Song song = new Song();
        song.setSonglistId(songlistId);
        song.setName(name.trim());
        song.setArtist(artist != null ? artist.trim() : "");
        song.setDuration(duration != null ? duration : 0);
        song.setUrl(url.trim());

        save(song);

        return Result.success(song);
    }

    @Override
    public Result<Song> getSongDetail(Long songId) {
        Song song = getById(songId);
        if (song == null) {
            return Result.fail("歌曲不存在");
        }
        return Result.success(song);
    }

    @Override
    public Result<String> deleteSong(Long userId, Long songId) {
        Song song = getById(songId);
        if (song == null) {
            return Result.fail("歌曲不存在");
        }

        Songlist songlist = songlistMapper.selectById(song.getSonglistId());
        if (songlist == null || !songlist.getOwnerId().equals(userId)) {
            return Result.fail("不能删除别人歌单里的歌曲");
        }

        removeById(songId);

        return Result.success("删除成功");
    }

    @Override
    public Result<List<Song>> getSonglistSongs(Long userId, Long songlistId) {
        Songlist songlist = songlistMapper.selectById(songlistId);
        if (songlist == null) {
            return Result.fail("歌单不存在");
        }

        LambdaQueryWrapper<Song> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Song::getSonglistId, songlistId)
               .eq(Song::getIsDeleted, 0);
        List<Song> songs = list(wrapper);

        return Result.success(songs);
    }
}
