package com.elevate.consultingplatform.repository;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true")
    Optional<User> findActiveById(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.role = :role AND (lower(u.email) LIKE lower(concat('%', :q, '%')) OR lower(u.firstName) LIKE lower(concat('%', :q, '%')) OR lower(u.lastName) LIKE lower(concat('%', :q, '%'))) ")
    Page<User> searchClients(@Param("role") Role role, @Param("q") String query, Pageable pageable);

    // Dashboard counts
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = :isActive")
    long countByRoleAndIsActive(@Param("role") Role role, @Param("isActive") boolean isActive);
}
