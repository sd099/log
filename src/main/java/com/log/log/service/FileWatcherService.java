package com.log.log.service;

import com.log.log.model.Message;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FileWatcherService {
    private static final String FILE_NAME = "log.txt";
    private static final String READ_MODE = "r";
    private static final String DESTINATION = "/topic/log";
    private long offset;

    private final RandomAccessFile randomAccessFile;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public FileWatcherService() throws IOException {
        randomAccessFile = new RandomAccessFile(FILE_NAME, READ_MODE);
        offset = randomAccessFile.length();
    }

    @Scheduled(fixedDelay = 100, initialDelay = 5000)
    public void sendUpdates() throws IOException {
        long fileLength = randomAccessFile.length();
        if (fileLength < offset) {
            offset = 0;
        }

        randomAccessFile.seek(offset);

        String line;
        while ((line = randomAccessFile.readLine()) != null) {
            String payload = "{\"content\":\"" + line + "\"}";
            messagingTemplate.convertAndSend(DESTINATION, payload);
            offset = randomAccessFile.getFilePointer();
        }
    }

    public List<Message> getLastTenLines() throws IOException {
        long length = randomAccessFile.length();
        long position = length;
        int lines = 0;
        List<Message> lastTenLines = new ArrayList<>();
        long lastNewLinePosition = length;

        while (position > 0) {
            position--;
            randomAccessFile.seek(position);
            if (randomAccessFile.readByte() == '\n') {
                if (lines < 10) {
                    lines++;
                    long currentLineStart = position + 1;
                    randomAccessFile.seek(currentLineStart);
                    String line = "{\"content\":\"" + randomAccessFile.readLine() + "\"}";
                    lastTenLines.add(0, new Message(line));
                    lastNewLinePosition = position;
                } else {
                    break;
                }
            }
        }

        // Handle the case where the first line in the file does not end with a newline
        if (lines < 10 && lastNewLinePosition > 0) {
            randomAccessFile.seek(0);
            String line = "{\"content\":\"" + randomAccessFile.readLine() + "\"}";
            lastTenLines.add(0, new Message(line));
        }

        return lastTenLines;
    }


}