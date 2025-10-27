package com.elevate.consultingplatform.controller.chat;

import com.elevate.consultingplatform.service.ai.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/chat", "/api/v1/chat"})
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat endpoint (authenticated)")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send a message and get a chat reply")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest body) {
        String reply = chatService.reply(body.getMessage());
        return ResponseEntity.ok(new ChatResponse(reply));
    }

    @Data
    public static class ChatRequest {
        private String message;
    }

    @Data
    public static class ChatResponse {
        private final String reply;
    }
}
