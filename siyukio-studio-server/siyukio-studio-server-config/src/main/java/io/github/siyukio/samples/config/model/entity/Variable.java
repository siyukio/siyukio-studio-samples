package io.github.siyukio.samples.config.model.entity;

import io.github.siyukio.tools.entity.postgresql.annotation.PgColumn;
import io.github.siyukio.tools.entity.postgresql.annotation.PgEntity;
import io.github.siyukio.tools.entity.postgresql.annotation.PgIndex;
import io.github.siyukio.tools.entity.postgresql.annotation.PgKey;
import lombok.Builder;
import lombok.With;

import java.time.LocalDateTime;

@Builder
@With
@PgEntity(
        schema = "siyukiostudio",
        comment = "Configuration variables",
        indexes = {
                @PgIndex(columns = {"category", "key"}, unique = true)
        }
)
public record Variable(

        @PgKey
        String id,

        @PgColumn
        String category,

        @PgColumn
        String description,

        @PgColumn
        String key,

        @PgColumn(encrypted = true)
        String value,

        @PgColumn
        String salt,

        @PgColumn
        boolean enabled,

        @PgColumn
        LocalDateTime createdAt,

        @PgColumn
        long createdAtTs,

        @PgColumn
        LocalDateTime updatedAt,

        @PgColumn
        long updatedAtTs

) {
}
