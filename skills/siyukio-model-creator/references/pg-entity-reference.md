# PostgreSQL Entity Reference for Siyukio

Use this file only when the task needs advanced annotation/DAO details.

## 1) `@PgEntity`

`comment` is required. Most other properties can stay default.

| Property | Default | Description |
| --- | --- | --- |
| `comment` | (none) | Table comment/description (required). |
| `schema` | `""` | Schema name (`""` usually maps to default schema). |
| `table` | `""` | Table name (`""` uses class-name snake_case). |
| `createTableAuto` | `true` | Auto-create table when missing. |
| `addColumnAuto` | `true` | Auto-add columns when missing. |
| `createIndexAuto` | `true` | Auto-create indexes when missing. |
| `dbName` | `""` | Database name for multi-db routing. |
| `partition` | `NONE` | Partition strategy (`NONE`, `YEAR`, `MONTH`, `DAY`, `HOUR`). |
| `keyInfo` | `""` | Encryption key context. |
| `indexes` | `{}` | Index definitions via `@PgIndex`. |
| `cacheConfig` | `@CacheConfig(maximumSize = 0)` | Entity cache config. `0` disables cache. |

### Cache fields (`cacheConfig`)

| Property | Default | Description |
| --- | --- | --- |
| `maximumSize` | `0` | Max entries; `0` disables cache. |
| `softValues` | `false` | Enable soft references. |
| `expireUnit` | `MINUTES` | Time unit for expiration values. |
| `expireAfterAccess` | `60` | Access-based expiry window. |
| `expireAfterWrite` | `15` | Write-based expiry window. |

## 2) `@PgKey`

```java
@PgKey
String id;

@PgKey(generated = false)
String id;
```

| Property | Default | Description |
| --- | --- | --- |
| `generated` | `true` | Auto-generate ID with GUID.v7() base64 on insert. |
| `comment` | `""` | Column comment. |

## 3) `@PgColumn`

| Property | Default | Description |
| --- | --- | --- |
| `encrypted` | `false` | Encrypt column value. |
| `comment` | `""` | Column comment. |

```java
@PgColumn
String name;

@PgColumn(encrypted = true)
String secretValue;

@PgColumn(comment = "business description")
String description;
```

## 4) Field mapping

camelCase fields map to snake_case columns automatically.

| Field | Column |
| --- | --- |
| `userName` | `user_name` |
| `createdAtTs` | `created_at_ts` |
| `id` | `id` |

## 5) Supported field types

- Basic: `String`, `boolean`, `int`, `long`, `double`
- Time: `LocalDateTime`, `long`
- JSON: `org.json.JSONObject`, `org.json.JSONArray`
- Collection: `List<T>`, `Set<T>`
- Enum fields
- Nested records (inner `record` + `@Builder` + `@With`)

## 6) Required timestamp fields

| Field | Type | Purpose |
| --- | --- | --- |
| `createdAt` | `LocalDateTime` | Human-readable create time |
| `createdAtTs` | `long` | Index-friendly create time |
| `updatedAt` | `LocalDateTime` | Human-readable update time |
| `updatedAtTs` | `long` | Index-friendly update time |

## 7) `PgEntityDao<T>` methods

### Create

- `insert(T t)`
- `insertBatch(Collection<T> tList)`

### Update

- `update(T t)`
- `updateBatch(Collection<T> tList)`
- `upsert(T t)`

### Delete

- `deleteById(Object id)`
- `delete(T t)`
- `deleteByQuery(QueryBuilder queryBuilder)`

### Query

- `existById(Object id)`
- `queryById(Object id)`
- `queryOne(QueryBuilder queryBuilder)`
- `queryList(QueryBuilder queryBuilder)`
- `queryList(QueryBuilder, SortBuilder)`
- `queryList(QueryBuilder, SortBuilder, int from, int size)`
- `queryList(int from, int size)`
- `queryList(SortBuilder sort)`
- `queryCount()`
- `queryCount(QueryBuilder queryBuilder)`
- `queryPage(QueryBuilder, SortBuilder, int page, int size)`

### Common supporting classes

- `QueryBuilder`, `QueryBuilders`
- `SortBuilder`, `SortBuilders`, `SortOrder`
- `Page<T>`
