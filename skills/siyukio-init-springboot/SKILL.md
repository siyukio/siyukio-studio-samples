---
name: siyukio-init-springboot
description: "Initialize a new server-side Siyukio Spring Boot Maven project from an empty directory/repo by creating a parent module plus {project-name}-common and {project-name}-bootstrap, Maven wrapper, baseline application.yml, and .gitignore. Use when asked to start/bootstrap a fresh Siyukio backend project."
---

# Goal

Create a ready-to-build Siyukio Spring Boot multi-module project skeleton.

# Scope

Use this skill for **server-side project initialization only**.

Do not use this skill for web/desktop/console tasks.

# Required inputs

- `{project-name}`: artifact/module prefix in kebab-case (example: `order-center`).
- `{package-name}`: base Java package (example: `io.github.siyukio.samples`).
- `{project-version}`: initial semantic version without `-SNAPSHOT` (example: `1.0.0`).

# Derived values

- `{package-path}`: `{package-name}` with dots replaced by `/`.
- `{ProjectName}`: PascalCase from `{project-name}` (example: `order-center` -> `OrderCenter`).
- `{main-class}`: `{ProjectName}Main`.

# Preconditions

- Target location is empty or contains only `.git` metadata.
- Java 21 and Maven 3.9+ are available.
- Output code/comments remain in English.

# Output structure

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
```

# Workflow

1. Validate inputs and derive `{package-path}` / `{ProjectName}` / `{main-class}`.
2. Create `./{project-name}`.
3. Create `./{project-name}/pom.xml` using the parent template.
4. Create `./{project-name}/{project-name}-common/pom.xml`.
5. Create `./{project-name}/{project-name}-bootstrap/pom.xml`.
6. Create `./{project-name}/{project-name}-bootstrap/src/main/java/{package-path}/{main-class}.java`.
7. Create `./{project-name}/{project-name}-bootstrap/src/main/resources/application.yml`.
8. Create `{project-name}-bootstrap/src/main/resources/application-local.yml` with exactly four flat key-value entries.
9. Create `./{project-name}/.gitignore`.
10. Run `cd ./{project-name} && mvn -N wrapper:wrapper`.
11. Verify with `./mvnw -q -DskipTests compile`.

If files already exist, merge conservatively and keep existing user customizations unless they block the required structure.

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
- Do not duplicate `application.yml` content

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

1. Run `./mvnw -q -DskipTests compile` in `./{project-name}`.
2. Optionally run `./mvnw -q -pl {project-name}-bootstrap spring-boot:run`.
3. Confirm startup logs show Spring Boot application started successfully.
