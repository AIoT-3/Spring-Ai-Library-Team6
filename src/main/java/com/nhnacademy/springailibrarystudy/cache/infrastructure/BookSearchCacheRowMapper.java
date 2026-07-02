package com.nhnacademy.springailibrarystudy.cache.infrastructure;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class BookSearchCacheRowMapper implements RowMapper<CachedResult> {

    @Override
    public CachedResult mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CachedResult(
                rs.getLong("id"),
                rs.getString("result"),
                rs.getDouble("similarity")
        );
    }
}