package com.elevate.consultingplatform.controller.publicapi;

import com.elevate.consultingplatform.service.ai.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/public/chat")
@RequiredArgsConstructor
@Tag(name = "Public Chat", description = "Public chatbot endpoint (rate-limited)")
public class PublicChatController {

    private final ChatService chatService;

    private static final Map<String, Deque<Long>> RATE_BUCKET = new ConcurrentHashMap<>();
    private static final int LIMIT = 10; // 10 requests
    private static final long WINDOW_MS = 60_000; // per 60 seconds

    @PostMapping
    @PermitAll
    @Operation(summary = "Send a message to the public chatbot (mock reply)")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest body, HttpServletRequest request) {
        String ip = clientKey(request);
        if (!allow(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        String reply = chatService.reply(body.getMessage());
        return ResponseEntity.ok(new ChatResponse(reply));
    }

    private String clientKey(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
        String ip = req.getRemoteAddr();
        return ip != null ? ip : "unknown";
    }

    private boolean allow(String key) {
        long now = Instant.now().toEpochMilli();
        Deque<Long> q = RATE_BUCKET.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && (now - q.peekFirst()) > WINDOW_MS) q.pollFirst();
            if (q.size() >= LIMIT) return false;
            q.addLast(now);
            return true;
        }
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
