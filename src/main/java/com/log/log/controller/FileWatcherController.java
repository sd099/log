package com.log.log.controller;

import com.log.log.model.Message;
import com.log.log.service.FileWatcherService;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FileWatcherController {
    @Autowired
    private FileWatcherService fileWatcherService;

    @GetMapping("/initialLog")
    @ResponseBody
    public List<String> getInitialLog() throws IOException {
        return fileWatcherService.getLastTenLines();
    }

    @MessageMapping("/logs")
    @SendTo("/topic/log")
    public Message getLogUpdates(Message message) {
        return message;
    }
}
