package com.zinc.zinctalk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zinc.zinctalk.entity.Songlist;
import com.zinc.zinctalk.mapper.SonglistMapper;
import com.zinc.zinctalk.service.SonglistService;
import org.springframework.stereotype.Service;

@Service
public class SonglistServiceImpl extends ServiceImpl<SonglistMapper, Songlist> implements SonglistService {
}
