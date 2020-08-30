package com.epicnerf.service;

import com.epicnerf.hibernate.dao.FirebaseTokenSupport;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class PushNotificationServiceImpl implements PushNotificationService {

    @Value("${firebase.server.key}")
    private String firebaseServerKey;
    @Value("${firebase.api.url}")
    private String firebaseApiUrl;

    @Autowired
    private FirebaseTokenSupport firebaseTokenSupport;

    public void sendPushNotification(List<String> keys, String messageTitle, String message) {
        JSONObject msg = new JSONObject();

        msg.put("title", messageTitle);
        msg.put("body", message);
        msg.put("icon", "/img/icons/manifest-icon-192.png");

        keys.forEach(key -> {
            String response = callToFcmServer(msg, key);
            if (response.contains("{\"error\":\"InvalidRegistration\"}")) {
                firebaseTokenSupport.removeUserMapping(key);
            }
        });
    }

    private String callToFcmServer(JSONObject message, String receiverFcmKey) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "key=" + firebaseServerKey);
        httpHeaders.set("Content-Type", "application/json");

        JSONObject json = new JSONObject();

        json.put("data", message);
        json.put("notification", message);
        json.put("to", receiverFcmKey);

        HttpEntity<String> httpEntity = new HttpEntity<>(json.toString(), httpHeaders);
        return restTemplate.postForObject(firebaseApiUrl, httpEntity, String.class);
    }
}
