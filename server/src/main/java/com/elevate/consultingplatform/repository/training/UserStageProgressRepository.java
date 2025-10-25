package com.elevate.consultingplatform.repository.training;

import com.elevate.consultingplatform.entity.training.UserStageProgress;
import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.catalog.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStageProgressRepository extends JpaRepository<UserStageProgress, Long> {
    List<UserStageProgress> findByUser(User user);
    Optional<UserStageProgress> findByUserAndStage(User user, Stage stage);
}
