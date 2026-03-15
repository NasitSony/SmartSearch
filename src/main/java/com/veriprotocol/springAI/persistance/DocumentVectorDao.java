package com.veriprotocol.springAI.persistance;

import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

@Repository
public class DocumentVectorDao {
	
	private final JdbcTemplate jdbc;

    public DocumentVectorDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
    
    public List<SearchRow> search(String queryVec, int k) {
        String sql = """
            SELECT id, text, status, content_hash, last_error, created_at, updated_at,
                   (embedding <-> CAST(? AS vector)) AS dist
            FROM documents
            WHERE embedding IS NOT NULL
            ORDER BY embedding <-> CAST(? AS vector)
            LIMIT ?
            """;

        return jdbc.query(sql,
                (rs, i) -> new SearchRow(
                        rs.getString("id"),
                        rs.getString("text"),
                        rs.getString("status"),
                        rs.getString("content_hash"),
                        rs.getString("last_error"),
                        rs.getObject("created_at", Instant.class),
                        rs.getObject("updated_at", Instant.class),
                        rs.getDouble("dist")
                ),
                queryVec, queryVec, k);
    }
    
    public record SearchRow(
            String id,
            String text,
            String status,
            String contentHash,
            String lastError,
            Instant createdAt,
            Instant updatedAt,
            double dist
    ) {}

}
