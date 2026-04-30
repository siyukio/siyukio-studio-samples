---
name: siyukio-init-springboot
description: Initialize a new Siyukio Spring Boot Maven backend from an empty directory/repo, including parent/common/bootstrap modules, Maven wrapper, baseline application configs, and .gitignore. Use when bootstrapping a fresh server project.
---

# siyukio-init-springboot

Bootstrap a ready-to-build Siyukio Spring Boot multi-module backend skeleton.

## Scope

Use this skill only for **new server initialization**.

Do not use for web/desktop/console tasks.

## Required inputs

- `{project-name}`: kebab-case module prefix (example: `order-center`)
- `{package-name}`: base package (example: `io.github.siyukio.samples`)
- `{project-version}`: semantic version without `-SNAPSHOT` (example: `1.0.0`)

## Derived values

- `{package-path}` = `{package-name}` with `.` replaced by `/`
- `{ProjectName}` = PascalCase of `{project-name}`
- `{main-class}` = `{ProjectName}Main`

## Preconditions

- Target directory is empty (or only contains `.git` metadata)
- Java 21 and Maven 3.9+ are available
- Generated code/comments stay in English

## Target structure

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
        └── resources/
            ├── application.yml
            └── application-local.yml
```

## Implementation rules

### 1) Parent `pom.xml`

- Inherit parent:
  - `groupId`: `io.github.siyukio`
  - `artifactId`: `spring-siyukio`
  - `version`: `3.5.14-M2`
- Set:
  - `groupId = {package-name}`
  - `artifactId = {project-name}`
  - `version = {project-version}-SNAPSHOT`
  - `packaging = pom`
  - Java source/target = `21`
- Modules:
  - `{project-name}-common`
  - `{project-name}-bootstrap`
- In `dependencyManagement`, include both module artifacts with `${project.version}`

### 2) `{project-name}-common/pom.xml`

- Parent points to root project (`{package-name}:{project-name}:{project-version}-SNAPSHOT`)
- `artifactId = {project-name}-common`

### 3) `{project-name}-bootstrap/pom.xml`

- Parent points to root project (`{package-name}:{project-name}:{project-version}-SNAPSHOT`)
- `artifactId = {project-name}-bootstrap`
- Dependencies:
  - `{package-name}:{project-name}-common`
  - `io.github.siyukio:spring-siyukio-application-acp`
- Add default `full` profile with property `deployment-profile=full`
- Configure `spring-boot-maven-plugin`
- `finalName = {project-name}-${deployment-profile}`

### 4) Main class

Create `{project-name}-bootstrap/src/main/java/{package-path}/{main-class}.java`:

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

### 5) `application.yml`

Create `{project-name}-bootstrap/src/main/resources/application.yml`:

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

### 6) `application-local.yml`

Create `{project-name}-bootstrap/src/main/resources/application-local.yml` with exactly four flat key-value entries.

- Source values from current repo `AGENTS.md` -> `Local Environment Configuration`
- Do not nest under `spring:`
- Do not duplicate `application.yml` content

Required keys:

```yaml
SIYUKIO_DB_MASTER_KEY: <value>
SIYUKIO_DB_MASTER_URL: <value>
SIYUKIO_DB_MASTER_USERNAME: <value>
SIYUKIO_DB_MASTER_PASSWORD: <value>
```

### 7) `.gitignore`

Include at least:

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

If files already exist, merge conservatively and keep user customizations unless they break required structure.

## Workflow

1. Validate inputs and derive `{package-path}`, `{ProjectName}`, `{main-class}`
2. Create `./{project-name}` and module directories
3. Write root/common/bootstrap POM files using rules above
4. Write main class and resource files
5. Write `.gitignore`
6. Run `cd ./{project-name} && mvn -N wrapper:wrapper`
7. Run `./mvnw -q -DskipTests compile`

## Verification

1. `./mvnw -q -DskipTests compile` succeeds in `./{project-name}`
2. `application-local.yml` exists with exactly four required keys from `AGENTS.md`
3. Optional runtime check: `./mvnw -q -pl {project-name}-bootstrap spring-boot:run`
