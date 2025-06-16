package org.app.eav.repo.command;

import org.app.eav.entity.Entity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntityRepo extends JpaRepository<Entity, Integer> {
}
