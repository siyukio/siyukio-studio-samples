---
name: siyukio-jwt-token-creator
description: Create or update JWT token creation and authorization issuance flows in Siyukio Spring Boot server modules using io.github.siyukio.tools.api.token.Token and TokenProvider. Use when implementing login token issuance, refresh-token to access-token conversion, app token distribution, or protected API token validation.
---

# siyukio-jwt-token-creator

Implement JWT flows with two explicit steps:
1. Create `Token`
2. Issue authorization string from `Token`

## Scope

Create or update code under:

```
{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/application/
{server-project-name}/{server-project-name}-{domain}/src/main/java/{package-path}/{domain}/api/
```

Typical files:

- `application/*Service.java` for token creation/issuance logic
- `api/*Controller.java` for login/refresh endpoints and token checks
- `api/dto/*Request.java` and `api/dto/*Response.java` for authorization payloads

## Required APIs

Use:

- `io.github.siyukio.tools.api.token.Token`
- `io.github.siyukio.tools.api.token.TokenProvider`

Inject provider in implementation classes:

```java
@Autowired
private TokenProvider tokenProvider;
```

## Step 1: Create JWT Token

Use `Token` to create the token object first.

### 1.1 Select token type

Use `token.type()` with:

- `Token.Type.ACCESS` for access token
- `Token.Type.REFRESH` for refresh token

Treat refresh token as non-access token. Never use `REFRESH` token directly to access protected APIs.

### 1.2 Select token principal

Use `token.principal()` with one of:

- `UserPrincipal.class`
- `AdminUserPrincipal.class`
- `AppPrincipal.class`
- `MemberPrincipal.class`
- `InternalPrincipal.class`

Default to user principal when no role/principal is explicitly required.

### 1.3 Create token examples

Create access token directly:

```java
Token accessToken = new Token(new Token.UserPrincipal("user-001", "user"));
```

Create refresh token first:

```java
Token refreshToken = new Token(
        new Token.UserPrincipal("user-001", "user"),
        Token.Type.REFRESH
);
```

Create access token from refresh token:

```java
Token accessToken = refreshToken.createAccessToken();
```

## Step 2: Issue JWT Authorization

Issue authorization string with `TokenProvider`.

Use default expiration:

```java
String authorization = tokenProvider.createAuthorization(token);
```

Use custom expiration only when explicitly required:

```java
String authorization = tokenProvider.createAuthorization(
        token,
        Duration.ofDays(360 * 100)
);
```

Default validity:

- ACCESS token: 15 minutes
- REFRESH token: 30 days

Do not override expiration unless requirement explicitly asks for custom duration.

## Common implementation scenarios

### User login

1. Create refresh token.
2. Issue refresh authorization.
3. Create access token from refresh token.
4. Issue access authorization.

Example:

```java
Token refreshToken = new Token(
        new Token.UserPrincipal("user-001", "user"),
        Token.Type.REFRESH
);
String refreshAuthorization = tokenProvider.createAuthorization(refreshToken);

Token accessToken = refreshToken.createAccessToken();
String accessAuthorization = tokenProvider.createAuthorization(accessToken);
```

### App token distribution

1. Create app access token.
2. Issue app authorization.

Example:

```java
Token appAccessToken = new Token(
        new Token.AppPrincipal("app-001", "studio-app"),
        Token.Type.ACCESS
);
String appAuthorization = tokenProvider.createAuthorization(appAccessToken);
```

## Validation rules for API usage

Verify inbound authorization with `tokenProvider.verifyToken(authorization)`.

- Reject when token is `null`.
- Reject when token type is `Token.Type.REFRESH` on protected APIs.
- Allow protected APIs only with `Token.Type.ACCESS`.

## Verification

After applying Java changes with this skill, run compile validation from `{server-project-name}/`.

Use Maven wrapper:

```bash
./mvnw -pl {server-project-name}-{domain} -DskipTests compile
```

Or use Maven:

```bash
mvn -pl {server-project-name}-{domain} -DskipTests compile
```

If auth-flow tests exist, run targeted tests after compile.
