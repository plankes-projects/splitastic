package com.epicnerf.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PushNotificationService {
    void sendPushNotification(List<String> keys, String messageTitle, String message);
}
