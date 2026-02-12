package com.example.sheetkv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.example.sheetkv.service.SyncService;

@SpringBootApplication
public class SheetKvApplication {
    private static final Logger logger = LoggerFactory.getLogger(SheetKvApplication.class);

    private final SyncService syncService;

    public SheetKvApplication(SyncService syncService) {
        this.syncService = syncService;
    }

    public static void main(String[] args) {
        SpringApplication.run(SheetKvApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        try {
            var result = syncService.flush();
            logger.info("flush.startup collections={} totalKeys={} duplicates={}",
                    result.collections(), result.totalKeys(), result.duplicates());
        } catch (Exception ex) {
            logger.error("flush.startup failed", ex);
        }
    }
}
