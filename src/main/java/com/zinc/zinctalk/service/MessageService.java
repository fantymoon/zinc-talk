package com.zinc.zinctalk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.Message;

import java.util.List;

public interface MessageService extends IService<Message> {
    Result<Message> saveMessage(Long senderId, Long roomId, String content);
    Result<List<Message>> getHistoryMessages(Long roomId, Long userId);
}
