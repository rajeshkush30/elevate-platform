package com.elevate.consultingplatform.controller.admin;

import com.elevate.consultingplatform.entity.assessment.ClientAssessment;
import com.elevate.consultingplatform.repository.assessment.ClientAssessmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/submissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Submissions", description = "List client assessment submissions with filters")
public class AdminSubmissionsController {

    private final ClientAssessmentRepository clientAssessmentRepository;

    @GetMapping
    @Operation(summary = "List submissions (paged)")
    public ResponseEntity<PageResponse<Item>> list(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size,
            @RequestParam(name = "sort", required = false, defaultValue = "createdAt,desc") String sort,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "stage", required = false) String stage,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to
    ) {
        // Build sort
        String[] sortParts = sort.split(",");
        Sort.Direction dir = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(dir, sortParts[0]);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sortObj);

        // Load page then filter in-memory (simple MVP)
        Page<ClientAssessment> pageData = clientAssessmentRepository.findAll(pageable);
        List<ClientAssessment> filtered = new ArrayList<>(pageData.getContent());

        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase(Locale.ROOT);
            filtered = filtered.stream().filter(ca -> {
                String name = Optional.ofNullable(ca.getAssessment()).map(a -> a.getName()).orElse("");
                String clientEmail = Optional.ofNullable(ca.getClient()).map(u -> u.getEmail()).orElse("");
                return name.toLowerCase(Locale.ROOT).contains(q) || clientEmail.toLowerCase(Locale.ROOT).contains(q);
            }).collect(Collectors.toList());
        }
        if (stage != null && !stage.isBlank()) {
            filtered = filtered.stream().filter(ca -> stage.equalsIgnoreCase(Objects.toString(ca.getStage(), ""))).collect(Collectors.toList());
        }
        if (status != null && !status.isBlank()) {
            filtered = filtered.stream().filter(ca -> status.equalsIgnoreCase(Objects.toString(ca.getStatus(), ""))).collect(Collectors.toList());
        }
        OffsetDateTime fromTs = parseTime(from);
        OffsetDateTime toTs = parseTime(to);
        if (fromTs != null) {
            filtered = filtered.stream().filter(ca -> ca.getCreatedAt() != null && !ca.getCreatedAt().isBefore(fromTs.toLocalDateTime())).collect(Collectors.toList());
        }
        if (toTs != null) {
            filtered = filtered.stream().filter(ca -> ca.getCreatedAt() != null && !ca.getCreatedAt().isAfter(toTs.toLocalDateTime())).collect(Collectors.toList());
        }

        List<Item> items = filtered.stream().map(AdminSubmissionsController::toItem).collect(Collectors.toList());

        // Compose response (retain requested paging size, but since we filtered in-memory, we return a single-page slice)
        PageResponse<Item> resp = new PageResponse<>();
        resp.setContent(items);
        resp.setTotalElements(items.size());
        resp.setTotalPages(1);
        resp.setNumber(page);
        resp.setSize(items.size());
        return ResponseEntity.ok(resp);
    }

    private static Item toItem(ClientAssessment ca) {
        Item it = new Item();
        it.setId(String.valueOf(ca.getId()));
        it.setUserEmail(Optional.ofNullable(ca.getClient()).map(u -> u.getEmail()).orElse(""));
        it.setStage(Objects.toString(ca.getStage(), ""));
        it.setScore(ca.getScore() != null ? ca.getScore().doubleValue() : 0.0);
        it.setStatus(Objects.toString(ca.getStatus(), "PENDING"));
        it.setCreatedAt(ca.getCreatedAt() != null ? ca.getCreatedAt().toString() : "");
        return it;
    }

    private static OffsetDateTime parseTime(String s) {
        if (s == null || s.isBlank()) return null;
        try { return OffsetDateTime.parse(s); } catch (DateTimeParseException e) { return null; }
    }

    @Data
    public static class Item {
        private String id;
        private String userEmail;
        private String stage;
        private double score;
        private String status;
        private String createdAt;
    }

    @Data
    public static class PageResponse<T> {
        private List<T> content;
        private long totalElements;
        private int totalPages;
        private int number;
        private int size;
    }
}
