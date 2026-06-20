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

    //上传
    @Override
    public Result<Song> uploadSong(Long userId, Long songlistId, String name,
                                   String artist, Integer duration, String url) {
        Result<String> check = checkWriteAccess(userId, songlistId, name, url);
        if (!check.getCode().equals(200)) return Result.fail(check.getMessage());

        Song song = new Song();
        song.setSonglistId(songlistId);
        song.setName(name.trim());
        song.setArtist(artist != null ? artist.trim() : "");
        song.setDuration(duration != null ? duration : 0);
        song.setUrl(url.trim());

        save(song);
        return Result.success(song);
    }

    //保存分享来的歌曲到自己的歌单
    @Override
    public Result<Song> saveSharedSong(Long userId, Long songlistId, String name,
                                       String artist, Integer duration, String url) {
        Result<String> check = checkWriteAccess(userId, songlistId, name, url);
        if (!check.getCode().equals(200)) return Result.fail(check.getMessage());

        Song song = new Song();
        song.setSonglistId(songlistId);
        song.setName(name.trim());
        song.setArtist(artist != null ? artist.trim() : "");
        song.setDuration(duration != null ? duration : 0);
        song.setUrl(url.trim());

        save(song);
        return Result.success(song);
    }

    //校验 歌单存在+属于本人+名字/路径非空
    private Result<String> checkWriteAccess(Long userId, Long songlistId, String name, String url) {
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
        return Result.success("ok");
    }

    //获取歌曲信息
    @Override
    public Result<Song> getSongDetail(Long songId) {
        Song song = getById(songId);
        if (song == null) {
            return Result.fail("歌曲不存在");
        }
        return Result.success(song);
    }

    //删除歌曲
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

    //获取歌单下的所有歌曲
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

    //更新歌曲
    @Override
    public Result<Song> updateSong(Long userId, Long songId, Song updateSong) {
        Song song = getById(songId);
        if (song == null) {
            return Result.fail("歌曲不存在");
        }

        Songlist songlist = songlistMapper.selectById(song.getSonglistId());
        if (songlist == null || !songlist.getOwnerId().equals(userId)) {
            return Result.fail("不能修改别人歌单里的歌曲");
        }

        if (updateSong.getName() == null || updateSong.getName().trim().isEmpty()) {
            return Result.fail("歌曲名不能为空");
        }

        song.setName(updateSong.getName().trim());
        if (updateSong.getArtist() != null) {
            song.setArtist(updateSong.getArtist().trim());
        }
        if (updateSong.getDuration() != null) {
            song.setDuration(updateSong.getDuration());
        }

        updateById(song);
        return Result.success(song);
    }
}
