package com.elevate.consultingplatform.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/admin/ai-prompts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAIPromptsController {

    public record PromptDto(String key, String text) {}

    // Temporary in-memory store (replace with persistence later)
    private static final Map<String, String> STORE = new ConcurrentHashMap<>();
    static {
        STORE.putIfAbsent("summary_prompt_v1", "You are a consulting AI. Produce a concise stage summary.");
        STORE.putIfAbsent("final_consultation_v1", "Draft a final consultation report based on strategy inputs.");
    }

    @GetMapping
    public ResponseEntity<List<PromptDto>> list() {
        List<PromptDto> res = new ArrayList<>();
        for (var e : STORE.entrySet()) res.add(new PromptDto(e.getKey(), e.getValue()));
        res.sort(Comparator.comparing(PromptDto::key));
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{key}")
    public ResponseEntity<PromptDto> update(@PathVariable String key, @RequestBody Map<String, String> body) {
        String text = body.getOrDefault("text", "");
        STORE.put(key, text);
        return ResponseEntity.ok(new PromptDto(key, text));
    }
}
