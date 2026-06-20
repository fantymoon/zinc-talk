package com.zinc.zinctalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.Song;
import com.zinc.zinctalk.entity.Songlist;
import com.zinc.zinctalk.mapper.SonglistMapper;
import com.zinc.zinctalk.mapper.SongMapper;
import com.zinc.zinctalk.service.SonglistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SonglistServiceImpl extends ServiceImpl<SonglistMapper, Songlist> implements SonglistService {

    @Autowired
    private SonglistMapper songlistMapper;

    @Autowired
    private SongMapper songMapper;

    //创建歌单
    @Override
    public Result<Songlist> createSonglist(Long userId, String name) {
        if (name == null || name.trim().isEmpty()) {
            return Result.fail("歌单名不能为空");
        }

        Songlist songlist = new Songlist();
        songlist.setOwnerId(userId);
        songlist.setName(name.trim());
        save(songlist);

        return Result.success(songlist);
    }

    //获取
    @Override
    public Result<List<Songlist>> getMySonglists(Long userId) {
        LambdaQueryWrapper<Songlist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Songlist::getOwnerId, userId)
                .eq(Songlist::getIsDeleted, 0);
        List<Songlist> songlists = list(wrapper);
        return Result.success(songlists);
    }

    //更新歌单
    @Override
    public Result<String> updateSonglist(Long userId, Long songlistId, String name) {
        if (name == null || name.trim().isEmpty()) {
            return Result.fail("歌单名不能为空");
        }

        Songlist songlist = getById(songlistId);
        if (songlist == null) {
            return Result.fail("歌单不存在");
        }

        if (!songlist.getOwnerId().equals(userId)) {
            return Result.fail("不能修改别人的歌单");
        }

        songlist.setName(name.trim());
        updateById(songlist);

        return Result.success("修改成功");
    }

    //删除歌单
    @Override
    public Result<String> deleteSonglist(Long userId, Long songlistId) {
        Songlist songlist = getById(songlistId);
        if (songlist == null) {
            return Result.fail("歌单不存在");
        }

        if (!songlist.getOwnerId().equals(userId)) {
            return Result.fail("不能删除别人的歌单");
        }

        //级联删除歌单下的所有歌曲
        LambdaQueryWrapper<Song> songWrapper = new LambdaQueryWrapper<>();
        songWrapper.eq(Song::getSonglistId, songlistId);
        songMapper.delete(songWrapper);

        removeById(songlistId);

        return Result.success("删除成功");
    }
}
