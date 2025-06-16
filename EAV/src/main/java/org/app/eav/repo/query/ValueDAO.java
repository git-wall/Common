package org.app.eav.repo.query;

import lombok.RequiredArgsConstructor;
import org.app.eav.entity.AttributeValue;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ValueDAO implements IValueDAO {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<AttributeValue> getAttributeValues() {
//        jdbcTemplate.execute();
        return List.of();
    }
}
