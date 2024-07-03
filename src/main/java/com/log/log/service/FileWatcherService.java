package com.log.log.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.RandomAccessFile;

@Component
public class FileWatcherService {
    private final static String FILE_NAME = "log.txt";
    private final static String READ_MODE = "r";
    public static final String DESTINATION = "/topic/log";
    private long offset;

    private final RandomAccessFile randomAccessFile;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public FileWatcherService() throws IOException {
        randomAccessFile = new RandomAccessFile(FILE_NAME, READ_MODE);

        offset = initialOffset();
    }

    @Scheduled(fixedDelay = 100, initialDelay = 5000)
    public void sendUpdates() throws IOException {
        long fileLength = randomAccessFile.length();

        randomAccessFile.seek(offset);

        while (randomAccessFile.getFilePointer() < fileLength) {
            String latestFileData = randomAccessFile.readLine();
            String payload = "{\"content\":\"" + latestFileData + "\"}";

            messagingTemplate
                    .convertAndSend(DESTINATION, payload);
        }

        offset = fileLength;
    }

    private long initialOffset() throws IOException {
        int lineCount = 0;

        while (randomAccessFile.readLine() != null) {
            lineCount++;
        }

        if(lineCount > 10) {
            offset = lineCount - 10;
        }

        return offset;
    }
}