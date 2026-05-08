---
name: siyukio-model-creator
description: "Create or modify Siyukio domain model-layer artifacts in server modules, including `{Entity}` in `model/entity`, `{Entity}Errors` in `model/errors`, and `{Entity}Policy` in `model/policy`. Use when asked to add or update entity/errors/policy fields, indexes, errors, or policies in a domain module."
---

# Goal

Create or modify the domain model layer for Siyukio Spring Boot modules using PostgreSQL, including entity, errors, and policy artifacts.

# Scope

Use this skill for:

- Creating new domain model-layer files: `{Entity}`, `{Entity}Errors`, and `{Entity}Policy`.
- Modifying existing entity fields/indexes and related errors/policy behavior.
- Keeping `model/entity`, `model/errors`, and `model/policy` aligned and consistent.

Do not use this skill for web/desktop/console tasks.

# Required inputs

- `{domain}`: domain module suffix in kebab-case (example: `user-management`).
- `{Entity}`: entity name in PascalCase (example: `User`).
- `{entity}`: entity variable name in camelCase (example: `user`).
- Entity fields: name, type, constraints, encrypted flag, nested record/enum needs.
- Indexes (only when explicitly required): column list and uniqueness.
- Optional table options (only when explicitly required): `dbName`, `schema`, `table`.
- Whether to generate Policy and Errors files.

# Preconditions

- Target module exists: `{server-project-name}/{server-project-name}-{domain}/`.
- Code stays in server modules only.
- Generated code/comments stay in English.

# Output files

- Entity:  
  `{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/model/entity/{Entity}.java`
- Optional errors:  
  `{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/model/errors/{Entity}Errors.java`
- Optional policy:  
  `{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/model/policy/{Entity}Policy.java`

# Workflow

1. Inspect target module and existing files.
2. Ensure dependency exists in `{server-project-name}-{domain}/pom.xml`:
   ```xml
   <dependency>
       <groupId>io.github.siyukio</groupId>
       <artifactId>spring-siyukio-postgresql</artifactId>
   </dependency>
   ```
3. Create or update `{Entity}` as a Java `record` with `@Builder`, `@With`, `@PgEntity`, `@PgKey`, `@PgColumn`, and `@PgIndex` entries only when explicitly required by the task.
   - Entity fields support enum types.
   - If a field needs fixed constants, define an internal `enum` inside `{Entity}` and use that enum as the field type.
4. Keep required timestamp fields: `createdAt`, `createdAtTs`, `updatedAt`, `updatedAtTs`.
5. If requested, create `{Entity}Errors` interface with constant names using `{ENTITY_UPPER}` pattern.
6. If requested, create `{Entity}Policy` component using `PgEntityDao<{Entity}>` for `checkExists` / `checkEnabled` / `findById`-style methods.
7. Verify compile and consistency.

# Entity template

```java
package {package-name}.{domain}.model.entity;

import io.github.siyukio.postgresql.entity.annotation.PgColumn;
import io.github.siyukio.postgresql.entity.annotation.PgEntity;
import io.github.siyukio.postgresql.entity.annotation.PgIndex;
import io.github.siyukio.postgresql.entity.annotation.PgKey;
import lombok.Builder;
import lombok.With;

import java.time.LocalDateTime;

@Builder
@With
@PgEntity(comment = "{tableComment}", indexes = {
        @PgIndex(columns = {"fieldA"}),
        @PgIndex(columns = {"fieldB", "fieldC"}, unique = true)
})
public record {Entity}(

        @PgKey
        String id,

        @PgColumn
        String name,

        @PgColumn
        Status status,

        @PgColumn
        LocalDateTime createdAt,

        @PgColumn
        long createdAtTs,

        @PgColumn
        LocalDateTime updatedAt,

        @PgColumn
        long updatedAtTs

) {
    public enum Status {
        ENABLED,
        DISABLED
    }
}
```

# Errors template (optional)

```java
package {package-name}.{domain}.model.errors;

public interface {Entity}Errors {

    String {ENTITY_UPPER}_NOT_FOUND = "{Entity} not found: %s";
    String {ENTITY_UPPER}_ALREADY_EXISTS = "{Entity} already exists: %s";
    String {ENTITY_UPPER}_DISABLED = "{Entity} is disabled: %s";
}
```

# Policy template (optional)

```java
package {package-name}.{domain}.model.policy;

import {package-name}.{domain}.model.errors.{Entity}Errors;
import {package-name}.{domain}.model.entity.{Entity};
import io.github.siyukio.tools.api.ApiException;
import io.github.siyukio.tools.entity.postgresql.PgEntityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class {Entity}Policy {

    @Autowired
    private PgEntityDao<{Entity}> {entity}Dao;

    public {Entity} check{Entity}Exists(String id) {
        {Entity} entity = this.{entity}Dao.queryById(id);
        if (entity == null) {
            throw new ApiException(String.format({Entity}Errors.{ENTITY_UPPER}_NOT_FOUND, id));
        }
        return entity;
    }

    public {Entity} check{Entity}Enabled(String id) {
        {Entity} entity = this.check{Entity}Exists(id);
        if (!entity.enabled()) {
            throw new ApiException(String.format({Entity}Errors.{ENTITY_UPPER}_DISABLED, id));
        }
        return entity;
    }

    public {Entity} findById(String id) {
        return this.{entity}Dao.queryById(id);
    }
}
```

# Conventions checklist

- Package root: `{package-name}.{domain}.model`.
- Entity package: `.entity`; errors package: `.errors`; policy package: `.policy`.
- Use Java `record` for entity DTO-style immutability.
- Apply `@PgColumn(encrypted = true)` only for sensitive fields.
- Unless explicitly required by the task, leave `@PgEntity` `dbName`, `schema`, and `table` unset.
- Omit `@PgColumn(comment = "...")` when the DTO field name is already clear and self-explanatory.
- Do not proactively create `@PgIndex` unless the current task explicitly requires it.
- Use `*Ts` (long) fields for index-friendly time queries.
- Unless explicitly required, do not create indexes on standalone time fields.
- Keep nested record types inside entity with `@Builder` and `@With`.
- For constant-like fields, prefer internal `enum` in `{Entity}` and use that enum as the field type.
- Keep Policy focused on validation/query helpers; business workflows belong to Application layer.

# Reference material

Load only when needed:

- `references/pg-entity-reference.md` for full `@PgEntity`, `@PgColumn`, `@PgKey`, DAO API, and supported type details.
- `../siyukio-application-creator/SKILL.md` for service-layer orchestration patterns.

# Verification

1. Run compile in project root or module root:
   ```bash
   ./mvnw compile
   ```
2. Confirm every field intended for persistence has `@PgColumn` (except computed-only fields).
3. Confirm index definitions match query paths.
4. Confirm policy method names and error constants align (`{ENTITY_UPPER}_...`).
