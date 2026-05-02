---
name: siyukio-common-creator
description: "Create or update global constant interfaces and utility abstract classes in Siyukio common module. Use when adding shared constants under `common/constants` or shared static helpers under `common/utils`."
---

# siyukio-common-creator

Create or update shared constants and utility helpers for the Siyukio common module.

## Scope

Working directory:

`{project-name}/{project-name}-common`

Target directories:

- Constants:
  `{project-name}/{project-name}-common/src/main/java/{package-path}/common/constants/`
- Utilities:
  `{project-name}/{project-name}-common/src/main/java/{package-path}/common/utils/`

## Use this skill when

- You need cross-module constant definitions.
- You need shared helper methods that do not keep state.
- You are adding or updating files in `common/constants` or `common/utils`.

## Do not use this skill when

- You are implementing domain business logic.
- You need non-static services/components managed by Spring.

## Required inputs

- `{project-name}`: root server project name.
- `{package-name}` and `{package-path}`.
- `{ConstantClass}`: constant type name in PascalCase (example: `UserConstants`).
- `{UtilityClass}`: utility type name in PascalCase (example: `DateTimeUtils`).
- Constant items or utility method contracts.

## Workflow

1. Normalize output paths and class names.
2. Create or update constants under `common/constants` as Java `interface calss`.
3. Create or update utility classes under `common/utils` as Java `abstract class`.
4. Keep utility methods static and stateless.
5. Run module compile verification.

## Constants rules

- File location:
  `.../common/constants/{ConstantClass}.java`
- Type declaration must be interface:
  `public interface {ConstantClass} { ... }`
- Use `UPPER_SNAKE_CASE` for constant names.
- Keep only constants; do not add mutable state.

Template:

```java
package {package-name}.common.constants;

public interface {ConstantClass} {

    String EXAMPLE_KEY = "example";
    int EXAMPLE_LIMIT = 100;
}
```

## Utility rules

- File location:
  `.../common/utils/{UtilityClass}.java`
- Type declaration must be abstract class:
  `public abstract class {UtilityClass} { ... }`
- All utility methods must be `static`.
- Keep methods side-effect-safe unless explicitly required.

Template:

```java
package {package-name}.common.utils;

public abstract class {UtilityClass} {

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
```

## Verification

From `{project-name}/` run:

```bash
./mvnw -pl {project-name}-common -DskipTests compile
```

Then confirm:

- Constants are in `common/constants` and declared as interfaces.
- Utilities are in `common/utils` and declared as abstract classes.
- Utility methods are static.
- Naming and package declarations match the target module.
