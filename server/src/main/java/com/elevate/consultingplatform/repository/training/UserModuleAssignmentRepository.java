package com.elevate.consultingplatform.repository.training;

import com.elevate.consultingplatform.entity.training.UserModuleAssignment;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.catalog.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserModuleAssignmentRepository extends JpaRepository<UserModuleAssignment, Long> {
    List<UserModuleAssignment> findByUser(User user);
    Optional<UserModuleAssignment> findByUserAndModule(User user, Module module);
}
