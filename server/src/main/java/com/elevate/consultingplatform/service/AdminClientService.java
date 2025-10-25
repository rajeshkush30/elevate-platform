package com.elevate.consultingplatform.service;

import com.elevate.consultingplatform.dto.user.UserResponse;
import com.elevate.consultingplatform.dto.user.CreateClientResponse;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminClientService {
    List<UserResponse> getAllClients();
    UserResponse getClientById(Long id);
    CreateClientResponse createClient(UserResponse request);
    UserResponse updateClient(Long id, UserResponse request);
    void deleteClient(Long id);
    Page<UserResponse> searchClients(String query, Pageable pageable);
    void resendInvite(Long id);
}
