package com.zinc.zinctalk.controller;

import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.Song;
import com.zinc.zinctalk.service.SongService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/song")
public class SongController {

    @Autowired
    private SongService songService;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<Song> uploadSong(@RequestParam("file") MultipartFile file,
                                   @RequestParam("songlistId") Long songlistId,
                                   @RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "artist", required = false) String artist,
                                   @RequestParam(value = "duration", required = false) Integer duration,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要上传的文件");
        }

        //没填歌曲名就用文件名
        String originalName = file.getOriginalFilename();
        if (name == null || name.trim().isEmpty()) {
            if (originalName != null && originalName.contains(".")) {
                name = originalName.substring(0, originalName.lastIndexOf('.'));
            } else {
                name = "未知歌曲";
            }
        }

        //生成新文件名
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.'))
                : ".mp3";
        String newFileName = java.util.UUID.randomUUID().toString().replace("-", "") + ext;

        //保存到磁盘
        File dest = new File(uploadDir + File.separator + "music" + File.separator + newFileName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest.getAbsoluteFile());
        } catch (IOException e) {
            return Result.fail("文件保存失败：" + e.getMessage());
        }

        //生成访问url
        String url = "/uploads/music/" + newFileName;

        //存数据库
        return songService.uploadSong(userId, songlistId, name, artist, duration, url);
    }

    //获取歌曲详情
    @GetMapping("/{songId}")
    public Result<Song> getSongDetail(@PathVariable Long songId) {
        return songService.getSongDetail(songId);
    }

    //删除歌曲
    @DeleteMapping("/{songId}")
    public Result<String> deleteSong(@PathVariable Long songId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return songService.deleteSong(userId, songId);
    }

    //修改歌曲信息
    @PutMapping("/{songId}")
    public Result<Song> updateSong(@PathVariable Long songId,
                                   @RequestBody Song song,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return songService.updateSong(userId, songId, song);
    }

    //获取歌单里的歌曲列表
    @GetMapping("/list/{songlistId}")
    public Result<List<Song>> getSonglistSongs(@PathVariable Long songlistId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return songService.getSonglistSongs(userId, songlistId);
    }
}
