---
name: siyukio-springboot-creator
description: "Create a new Siyukio Spring Boot Maven project or update configuration files in an existing Siyukio Spring Boot project. Use when asked to bootstrap a fresh backend skeleton, add missing Spring Boot baseline config, or merge/update `application.yml`, `application-local.yml`, and related Maven/gitignore config in an existing backend."
---

# Goal

Create or align a Siyukio Spring Boot backend with the expected baseline structure and configuration.

# Scope

Use this skill for server-side Spring Boot work only.

Do not use this skill for web/desktop/console tasks.

# Modes

## Mode A: Bootstrap

Create a new multi-module Spring Boot skeleton in an empty target directory.

## Mode B: Config update

Update or backfill config files for an existing Spring Boot project without replacing user business code.

# Required inputs

## Common inputs

- `{project-name}`: artifact/module prefix in kebab-case (example: `order-center`).
- `{package-name}`: base Java package (example: `io.github.siyukio.samples`).
- `{project-version}`: initial semantic version without `-SNAPSHOT` (example: `1.0.0`).
- `{project-root}`: absolute or repo-relative path to the target project root (bootstrap default: `./{project-name}`).

# Derived values

- `{package-path}`: `{package-name}` with dots replaced by `/`.
- `{ProjectName}`: PascalCase from `{project-name}` (example: `order-center` -> `OrderCenter`).
- `{main-class}`: `{ProjectName}Main`.

# Preconditions

- Java 21 and Maven 3.9+ are available.
- Output code/comments remain in English.

# Mode selection

Select mode before editing:

1. Choose **Mode A (Bootstrap)** when `{project-root}` is empty or only has VCS metadata.
2. Choose **Mode B (Config update)** when `{project-root}` already contains a Spring Boot project (`pom.xml`, `src/main/resources`, or existing `application*.yml`).
3. If uncertain, inspect files first and select the least destructive path.

# Output structure (Mode A)

```text
{project-name}/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .mvn/
├── .gitignore
├── {project-name}-common/
│   └── pom.xml
└── {project-name}-bootstrap/
    ├── pom.xml
    └── src/main/
        ├── java/{package-path}/{main-class}.java
        └── resources/application.yml
        └── resources/application-local.yml
```

# Workflow

## Mode A: Bootstrap workflow

1. Validate inputs and derive `{package-path}` / `{ProjectName}` / `{main-class}`.
2. Create `{project-root}`.
3. Create `{project-root}/pom.xml` using the parent template.
4. Create `{project-root}/{project-name}-common/pom.xml`.
5. Create `{project-root}/{project-name}-bootstrap/pom.xml`.
6. Create `{project-root}/{project-name}-bootstrap/src/main/java/{package-path}/{main-class}.java`.
7. Create `{project-root}/{project-name}-bootstrap/src/main/resources/application.yml`.
8. Create `{project-root}/{project-name}-bootstrap/src/main/resources/application-local.yml` with exactly four flat key-value entries.
9. Create `{project-root}/.gitignore`.
10. Run `cd {project-root} && mvn -N wrapper:wrapper`.
11. Verify with `./mvnw -q -DskipTests compile`.

## Mode B: Config update workflow

1. Inspect and confirm target files under `{project-root}`:
   - `pom.xml`
   - `{project-name}-bootstrap/pom.xml` or module-specific bootstrap `pom.xml`
   - `src/main/resources/application.yml` (or module equivalent)
   - `src/main/resources/application-local.yml` (or module equivalent)
   - `.gitignore`
2. Back up intent by reading existing file content first. Do not overwrite entire files.
3. Merge baseline keys into `application.yml`:
   - Add missing `management.endpoints.web.exposure.include`.
   - Add missing `spring.siyukio.jwt.*`, `spring.siyukio.signature.salt`, `spring.siyukio.profiles.*`.
   - Add missing `spring.datasource.postgres.*` tree.
   - Add missing `server.port`.
4. Merge or create `application-local.yml` as flat key-value entries:
   - `SIYUKIO_DB_MASTER_KEY`
   - `SIYUKIO_DB_MASTER_URL`
   - `SIYUKIO_DB_MASTER_USERNAME`
   - `SIYUKIO_DB_MASTER_PASSWORD`
5. Merge `.gitignore` entries if missing:
   - `**/target/`
   - `/**/application-local.yml`
   - `.mvn/wrapper/maven-wrapper.jar`
6. Check Maven files:
   - Keep existing versions unless user explicitly asks to upgrade.
   - Ensure local module references remain consistent.
   - Add missing `spring-boot-maven-plugin` only when the bootstrap module lacks build packaging support.
7. Verify with project-local wrapper command:
   - If `./mvnw` exists: `./mvnw -q -DskipTests compile`
   - Otherwise: `mvn -q -DskipTests compile`

# Merge policy (Mode B)

Apply conservative merges:

1. Preserve existing user keys unless they directly conflict with required baseline keys.
2. Update only requested or baseline-owned keys; do not rewrite unrelated sections.
3. Keep file format stable (YAML stays YAML, indentation stays two spaces unless project differs).
4. If a key exists with a non-empty custom value, keep it unless the user explicitly asks to replace it.
5. If a required key exists but is structurally incompatible, report and apply the minimal compatible fix.

# Templates

## Parent `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>io.github.siyukio</groupId>
        <artifactId>spring-siyukio</artifactId>
        <version>3.5.14-M2</version>
    </parent>

    <groupId>{package-name}</groupId>
    <artifactId>{project-name}</artifactId>
    <version>{project-version}-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>{ProjectName}</name>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <modules>
        <module>{project-name}-common</module>
        <module>{project-name}-bootstrap</module>
    </modules>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>{package-name}</groupId>
                <artifactId>{project-name}-common</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>{package-name}</groupId>
                <artifactId>{project-name}-bootstrap</artifactId>
                <version>${project.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

## `{project-name}-common/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>{package-name}</groupId>
        <artifactId>{project-name}</artifactId>
        <version>{project-version}-SNAPSHOT</version>
    </parent>

    <artifactId>{project-name}-common</artifactId>
    <name>{ProjectName} Common</name>
</project>
```

## `{project-name}-bootstrap/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>{package-name}</groupId>
        <artifactId>{project-name}</artifactId>
        <version>{project-version}-SNAPSHOT</version>
    </parent>

    <artifactId>{project-name}-bootstrap</artifactId>
    <name>{ProjectName} Bootstrap</name>

    <dependencies>
        <dependency>
            <groupId>{package-name}</groupId>
            <artifactId>{project-name}-common</artifactId>
        </dependency>
        <dependency>
            <groupId>io.github.siyukio</groupId>
            <artifactId>spring-siyukio-application-acp</artifactId>
        </dependency>
    </dependencies>

    <profiles>
        <profile>
            <id>full</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <deployment-profile>full</deployment-profile>
            </properties>
        </profile>
    </profiles>

    <build>
        <finalName>{project-name}-${deployment-profile}</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## Main class

```java
package {package-name};

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class {main-class} {

    public static void main(String[] args) {
        new SpringApplicationBuilder({main-class}.class)
                .build()
                .run(args);
    }
}
```

## `application.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health

spring:
  siyukio:
    jwt:
      public-key: ${SIYUKIO_JWT_PUBLIC_KEY:}
      private-key: ${SIYUKIO_JWT_PRIVATE_KEY:}
      password: ${SIYUKIO_JWT_PASSWORD:}
    signature:
      salt: ${SIYUKIO_SIGNATURE_SALT:siyukio}
    profiles:
      docs: ${SIYUKIO_PROFILES_DOCS:true}
      active: ${SIYUKIO_PROFILES_ACTIVE:dev}
  datasource:
    postgres:
      master-key: ${SIYUKIO_DB_MASTER_KEY:}
      hikari:
        maximum-pool-size: 18
        minimum-idle: 18
        idle-timeout: 600000
        max-lifetime: 1800000
      master:
        url: ${SIYUKIO_DB_MASTER_URL:jdbc:postgresql://localhost:5432/root}
        username: ${SIYUKIO_DB_MASTER_USERNAME:}
        password: ${SIYUKIO_DB_MASTER_PASSWORD:}

server:
  port: ${SERVER_PORT:8080}
```

## `application-local.yml`

- Source values from current repo `AGENTS.md` -> `Local Environment Configuration`
- Do not nest under `spring:`
- Keep this file as flat environment-style overrides
- In Mode B, preserve additional user-owned local keys unless asked to prune them

Required keys:

```yaml
SIYUKIO_DB_MASTER_KEY: <value>
SIYUKIO_DB_MASTER_URL: <value>
SIYUKIO_DB_MASTER_USERNAME: <value>
SIYUKIO_DB_MASTER_PASSWORD: <value>
```

## `.gitignore`

```gitignore
**/target/

/**/application-local.yml

.omx/

.idea/
*.iml
.vscode/
*.swp
*.swo
*~

.DS_Store
Thumbs.db

.mvn/wrapper/maven-wrapper.jar
.settings/
.project
.classpath
```

# Verification checklist

1. Run compile check in target root:
   - `./mvnw -q -DskipTests compile` (preferred)
   - `mvn -q -DskipTests compile` (fallback)
2. Optionally run `./mvnw -q -pl {project-name}-bootstrap spring-boot:run`.
3. Confirm startup logs show Spring Boot application started successfully.
4. Confirm config merge result:
   - No unrelated YAML sections removed.
   - Required Siyukio keys exist after merge.
   - Existing non-conflicting custom keys still exist.
