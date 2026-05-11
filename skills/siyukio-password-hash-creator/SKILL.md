---
name: siyukio-password-hash-creator
description: Create or update password hashing and password-login verification logic in Siyukio Spring Boot server modules. Use when implementing registration, password reset/change, or password login so raw passwords are encoded with PasswordEncoder.encode before storage and verified with PasswordEncoder.matches during authentication.
---

# siyukio-password-hash-creator

Apply a secure and consistent password-hash workflow in Siyukio server modules.

## Scope

Use this skill for:

- Creating or updating save/update flows that write user passwords.
- Creating or updating password-login verification flows.
- Refactoring legacy `equals` password checks to `PasswordEncoder.matches`.
- Standardizing password handling in `application`, auth-related services, and module security configuration.

Do not use this skill for:

- OAuth-only / token-only authentication paths with no password handling.
- UI-only password form changes without backend password persistence or verification logic.

## Required rules

1. Ensure module `pom.xml` includes `spring-security-crypto` and `bcprov-jdk18on` when password hash or password validation logic is involved.
2. Ensure module has a `PasswordEncoder` bean initialization in `{domain}/configuration/SecurityConfiguration.java`.
3. Always inject `org.springframework.security.crypto.password.PasswordEncoder` with `@Autowired` before handling password hashing or password matching.
4. Always hash raw passwords with `PasswordEncoder.encode(String rawPassword)` before storing.
5. Always verify login with `PasswordEncoder.matches(String rawPassword, String encodedPassword)`.
6. Never compare raw password and stored hash using `equals`.
7. Never store raw passwords and never log raw passwords or password hashes.
8. Avoid double hashing. Encode only user-provided raw passwords from request input.

## Execution workflow

### 1) Check module dependencies in `pom.xml`

Inspect:

- `{server-project-name}/{server-project-name}-{domain}/pom.xml`

If dependencies are missing, add:

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
</dependency>
```

### 2) Ensure PasswordEncoder bean initialization exists

Inspect:

- `{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/configuration/SecurityConfiguration.java`

If not initialized, create/update with:

```java
@Configuration
@Slf4j
public class SecurityConfiguration {

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        Argon2PasswordEncoder argon2PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        String hash = argon2PasswordEncoder.encode("init");
        log.info("Init argon2PasswordEncoder v5_8: init, {}", hash);
        return argon2PasswordEncoder;
    }
}
```

### 3) Hash password before persistence via injected `PasswordEncoder`

Inject once:

```java
@Autowired
private PasswordEncoder passwordEncoder;
```

When handling save/reset/change password:

```java
public void doBusiness(String passwordPlaintext) {
    String passwordHash = this.passwordEncoder.encode(passwordPlaintext);
    // Save passwordHash
}
```

### 4) Verify login password with `matches` via injected `PasswordEncoder`

Inject once:

```java
@Autowired
private PasswordEncoder passwordEncoder;
```

For login verification:

```java
public void doBusiness(String passwordPlaintext, String passwordHash) {
    if (!this.passwordEncoder.matches(passwordPlaintext, passwordHash)) {
        throw new ApiException("Invalid password");
    }
}
```

### Apply guardrails

- Do not query user by password hash generated from login input.
- Do not re-encode already encoded values from database.
- Keep password fields out of API response DTOs.
- Keep error messages generic for login failures.

## Verification

Run module compile:

```bash
./mvnw -pl {server-project-name}-{domain} -DskipTests compile
```

If auth tests exist, run targeted tests:

```bash
./mvnw -pl {server-project-name}-{domain} test -Dtest=*Auth*Test,*User*ServiceTest
```

Then confirm:

- New/updated password writes always call `passwordEncoder.encode`.
- Login verification always calls `passwordEncoder.matches`.
- No `password.equals(...)` comparison remains in password-auth code paths.
- No plaintext password is persisted or returned by API DTO.
