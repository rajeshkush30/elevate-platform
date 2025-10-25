package com.elevate.consultingplatform.controller;

import com.elevate.consultingplatform.dto.user.UserResponse;
import com.elevate.consultingplatform.service.AdminClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/admin/clients")
@RequiredArgsConstructor
@Tag(name = "Admin Clients", description = "Admin client management endpoints")
public class AdminClientController {

    private final AdminClientService adminClientService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all clients")
    public ResponseEntity<List<UserResponse>> list() {
        return ResponseEntity.ok(adminClientService.getAllClients());
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search clients with pagination")
    public ResponseEntity<Page<UserResponse>> search(
            @RequestParam(name = "query", required = false, defaultValue = "") String query,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false, defaultValue = "createdAt,desc") String sort
    ) {
        String[] sortParts = sort.split(",");
        Sort.Direction dir = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(dir, sortParts[0]);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sortObj);
        return ResponseEntity.ok(adminClientService.searchClients(query, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get client by id")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(adminClientService.getClientById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a client")
    public ResponseEntity<com.elevate.consultingplatform.dto.user.CreateClientResponse> create(@RequestBody UserResponse request) {
        return ResponseEntity.ok(adminClientService.createClient(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a client")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody UserResponse request) {
        return ResponseEntity.ok(adminClientService.updateClient(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a client")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminClientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resend-invite")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resend invite email (password reset) to client")
    public ResponseEntity<Void> resendInvite(@PathVariable Long id) {
        adminClientService.resendInvite(id);
        return ResponseEntity.ok().build();
    }
}
