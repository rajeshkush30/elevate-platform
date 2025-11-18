package com.elevate.consultingplatform.controller.admin;

import com.elevate.consultingplatform.entity.assessment.ClientAssessment;
import com.elevate.consultingplatform.repository.assessment.ClientAssessmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
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
        // Build sort and pageable
        String[] sortParts = sort.split(",");
        Sort.Direction dir = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortProp = sortParts[0];
        // allow only safe sort properties
        Set<String> allowedSort = Set.of("createdAt", "updatedAt", "score", "stage", "status", "id");
        if (!allowedSort.contains(sortProp)) sortProp = "createdAt";
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(dir, sortProp));

        // Build specification
        Specification<ClientAssessment> spec = Specification.where(null);
        if (query != null && !query.isBlank()) {
            String q = query.trim().toLowerCase(Locale.ROOT);
            String finalSortProp = sortProp; // capture for lambda
            spec = spec.and((root, cq, cb) -> {
                var assessmentJoin = root.join("assessment");
                var clientJoin = root.join("client");
                var nameLike = cb.like(cb.lower(assessmentJoin.get("name")), "%" + q + "%");
                var emailLike = cb.like(cb.lower(clientJoin.get("email")), "%" + q + "%");
                return cb.or(nameLike, emailLike);
            });
        }
        if (stage != null && !stage.isBlank()) {
            String st = stage;
            spec = spec.and((root, cq, cb) -> cb.equal(cb.lower(root.get("stage")), st.toLowerCase(Locale.ROOT)));
        }
        if (status != null && !status.isBlank()) {
            String st = status;
            spec = spec.and((root, cq, cb) -> cb.equal(cb.lower(root.get("status")), st.toLowerCase(Locale.ROOT)));
        }
        OffsetDateTime fromTs = parseTime(from);
        if (fromTs != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fromTs.toLocalDateTime()));
        }
        OffsetDateTime toTs = parseTime(to);
        if (toTs != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), toTs.toLocalDateTime()));
        }

        Page<ClientAssessment> pageData = clientAssessmentRepository.findAll(spec, pageable);
        List<Item> items = pageData.getContent().stream().map(AdminSubmissionsController::toItem).collect(Collectors.toList());

        PageResponse<Item> resp = new PageResponse<>();
        resp.setContent(items);
        resp.setTotalElements(pageData.getTotalElements());
        resp.setTotalPages(pageData.getTotalPages());
        resp.setNumber(pageData.getNumber());
        resp.setSize(pageData.getSize());
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
