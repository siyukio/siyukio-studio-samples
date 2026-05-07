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
- Standardizing password handling in `application` and auth-related services.

Do not use this skill for:

- OAuth-only / token-only authentication paths with no password handling.
- UI-only password form changes without backend password persistence or verification logic.

## Required rules

1. Always inject `org.springframework.security.crypto.password.PasswordEncoder` with `@Autowired` before handling password hashing or password matching.
2. Always hash raw passwords with `PasswordEncoder.encode(String rawPassword)` before storing.
3. Always verify login with `PasswordEncoder.matches(String rawPassword, String encodedPassword)`.
4. Never compare raw password and stored hash using `equals`.
5. Never store raw passwords and never log raw passwords or password hashes.
6. Avoid double hashing. Encode only user-provided raw passwords from request input.

## Execution workflow

### 1) Locate password touch points

Inspect service flows that:

- Create user/account credentials
- Reset/change passwords
- Perform password login checks

Typical files:

- `{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/application/*Service.java`(only when password checks happen here)

### 2) Inject PasswordEncoder

Import and inject `PasswordEncoder`:

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Autowired
private PasswordEncoder passwordEncoder;
```

### 3) Hash before persistence

For create/reset/change password paths:

```java
String passwordHash = this.passwordEncoder.encode(request.password());

User saved = this.userDao.insert(
        User.builder()
                .username(request.username())
                .password(passwordHash)
                .build()
);
```

If updating existing entity:

```java
String passwordHash = this.passwordEncoder.encode(request.newPassword());
User updated = this.userDao.updateById(user.withPassword(passwordHash));
```

### 4) Verify login with matches

For password login:

```java
User user = this.userPolicy.checkUserExistsByUsername(request.username());
if (!this.passwordEncoder.matches(request.password(), user.password())) {
    throw new ApiException("Invalid username or password");
}
```

### 5) Apply guardrails

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
