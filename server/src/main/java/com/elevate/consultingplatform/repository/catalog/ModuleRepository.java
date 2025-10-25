package com.elevate.consultingplatform.repository.catalog;

import com.elevate.consultingplatform.entity.catalog.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
}
