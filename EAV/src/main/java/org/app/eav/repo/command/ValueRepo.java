package org.app.eav.repo.command;

import org.app.eav.entity.Value;
import org.app.eav.repo.dao.IValueDAO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValueRepo extends JpaRepository<Value, Long>, IValueDAO {
}
