package com.elevate.consultingplatform.service.ai.impl;

import com.elevate.consultingplatform.service.ai.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!live")
public class ChatServiceImpl implements ChatService {

    @Override
    public String reply(String message) {
        if (message == null || message.isBlank()) {
            return "Hello! I'm your assistant. Ask me anything.";
        }
        // Simple mock echo reply
        return "[MOCK] You said: " + message;
    }
}
