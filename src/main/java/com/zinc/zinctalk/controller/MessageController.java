package com.zinc.zinctalk.controller;

import com.zinc.zinctalk.common.Result;
import com.zinc.zinctalk.entity.Message;
import com.zinc.zinctalk.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/{roomId}")
    public Result<List<Message>> getHistoryMessages(@PathVariable Long roomId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return messageService.getHistoryMessages(roomId, userId);
    }
}
