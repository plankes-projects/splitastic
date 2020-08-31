package com.epicnerf.service;

import com.epicnerf.databean.NotificationRoutingInfo;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PushNotificationService {
    void sendPushNotification(@NonNull NotificationRoutingInfo info, List<String> keys, String messageTitle, String message);
}
