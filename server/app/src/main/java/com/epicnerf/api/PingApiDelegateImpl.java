package com.epicnerf.api;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PingApiDelegateImpl implements PingApiDelegate {
    @Override
    public ResponseEntity<String> pingGet() {
        return ResponseEntity.ok("pong");
    }
}
