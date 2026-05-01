---
name: siyukio-acp-creator
description: Create or update ACP (Agent Client Protocol) session handler implementations in Siyukio Spring Boot projects. Use when adding or modifying AcpSessionHandlerImpl or @Bean acpSessionHandler(...) for session lifecycle handling, session mode/model state management, cancellation behavior, prompt handling, or ACP tool-call progress/result signaling over WebSocket.
---

# siyukio-acp-creator

Create or refine ACP session handler implementations with safe ownership checks and explicit session mode/model handling.

## Scope

Create or update files under:

```
{project-name}/{project-name}-agent/src/main/java/{package-path}/agent/application/acp/
├── AcpSessionHandlerImpl.java
└── AcpSessionConfiguration.java (optional, for @Bean acpSessionHandler)
```

Optional supporting files when persistence is required:

```
{project-name}/{project-name}-agent/src/main/java/{package-path}/agent/domain/... (services/repositories)
```

## Use this skill when

- Add a new `AcpSessionHandler` implementation.
- Modify existing `AcpSessionHandlerImpl` behavior.
- Modify an existing `@Bean AcpSessionHandler acpSessionHandler(...)` configuration bean.
- Add/adjust session mode/model state switching behavior.
- Add/adjust prompt/cancel lifecycle handling.

## Do not use this skill when

- Work is API endpoint exposure only. Use `$siyukio-api-creator`.
- Work is domain model/policy design only. Use `$siyukio-domain-creator`.
- Work is application orchestration unrelated to ACP session lifecycle. Use `$siyukio-application-creator`.

## Preconditions

- Use server modules only.
- Ensure target module exists: `{project-name}/{project-name}-agent/`.
- Ensure dependency exists in `{project-name}-agent/pom.xml`:

```xml
<dependency>
  <groupId>io.github.siyukio</groupId>
  <artifactId>spring-siyukio-application-acp</artifactId>
</dependency>
```

## Required inputs

- `{Agent}`: business name in PascalCase (for class/log context).
- Handler style:
  - `class`: `AcpSessionHandlerImpl implements AcpSessionHandler`
  - `bean`: `@Bean AcpSessionHandler acpSessionHandler(...)`
- Supported mode IDs (example: `default`, `code`, `review`).
- Supported model IDs (example: `default`, `gpt-5`).
- Session storage mode: in-memory or persisted.
- Whether `handlePrompt` requires domain integration now or placeholder response.

## Execution workflow

### 1) Locate existing handler first

Search for both implementation styles before editing:

- `implements AcpSessionHandler`
- `AcpSessionHandler acpSessionHandler(`

Rules:

- If handler exists, modify in place and preserve existing domain behavior.
- If both styles exist, follow the active style already used in the module.
- Do not create duplicate ACP handler beans.

### 2) Create or update handler skeleton

Package target: `{package-name}.agent.application.acp`.

Class style (preferred for domain-agent modules):

- Annotate with `@Service` and `@Slf4j`.
- Implement `AcpSessionHandler`.

Bean style (for configuration-driven modules):

- Keep `@Configuration` class.
- Provide `@Bean` method named `acpSessionHandler` when that convention already exists.
- Return either concrete `AcpSessionHandlerImpl` or anonymous `AcpSessionHandler` with equivalent behavior.

### 3) Implement lifecycle behavior

Method rules:

- Keep `Token token` as the first parameter in all overridden methods.
- `handleInit`: return `AcpSchema.InitializeResponse.ok()` unless custom init validation is required.
- `handleNewSession`: initialize session mode/model states and return a deterministic session ID.
- `handleLoadSession`: reject unknown or cross-user session IDs with `AcpProtocolException`.
- `handlePrompt`: if integrated, send thought/message/tool events through `AcpSessionContext`; otherwise return explicit placeholder response.
- `handleCancel`: stop in-flight work and clean temporary resources if any.
- `handleSetSessionMode` / `handleSetSessionModel`: validate requested IDs against supported lists before applying updates.

### 4) Session state strategy

- In-memory mode: derive state from request/token and local runtime map.
- Persisted mode: load/save session state through domain services/repositories.
- Keep `sessionId`, `currentModeId`, and `currentModelId` consistent across new/load/set operations.
- Reject invalid mode/model transitions early with protocol errors.

## Minimal class template (create or replace)

```java
package {package-name}.agent.application.acp;

import com.agentclientprotocol.sdk.error.AcpProtocolException;
import com.agentclientprotocol.sdk.spec.AcpSchema;
import io.github.siyukio.application.acp.AcpSessionHandler;
import io.github.siyukio.tools.acp.AcpSessionContext;
import io.github.siyukio.tools.api.token.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AcpSessionHandlerImpl implements AcpSessionHandler {

    @Override
    public AcpSchema.InitializeResponse handleInit(Token token, AcpSchema.InitializeRequest req) {
        return AcpSchema.InitializeResponse.ok();
    }

    @Override
    public AcpSchema.NewSessionResponse handleNewSession(Token token, AcpSchema.NewSessionRequest req) {
        AcpSchema.SessionModeState modeState = new AcpSchema.SessionModeState("default", List.of());
        AcpSchema.SessionModelState modelState = new AcpSchema.SessionModelState("default", List.of());
        return new AcpSchema.NewSessionResponse(token.id(), modeState, modelState);
    }

    @Override
    public AcpSchema.LoadSessionResponse handleLoadSession(Token token, AcpSchema.LoadSessionRequest req) {
        if (!Objects.equals(token.id(), req.sessionId())) {
            throw new AcpProtocolException(HttpStatus.NOT_FOUND.value(), "Session not found: " + req.sessionId());
        }
        AcpSchema.SessionModeState modeState = new AcpSchema.SessionModeState("default", List.of());
        AcpSchema.SessionModelState modelState = new AcpSchema.SessionModelState("default", List.of());
        return new AcpSchema.LoadSessionResponse(modeState, modelState);
    }

    @Override
    public AcpSchema.PromptResponse handlePrompt(Token token, AcpSchema.PromptRequest req, AcpSessionContext acpSessionContext) {
        acpSessionContext.sendThought("Processing...");
        return AcpSchema.PromptResponse.endTurn();
    }

    @Override
    public void handleCancel(Token token, AcpSchema.CancelNotification req) {
        log.info("Cancel session: {}", req.sessionId());
    }

    @Override
    public AcpSchema.SetSessionModeResponse handleSetSessionMode(Token token, AcpSchema.SetSessionModeRequest req) {
        return new AcpSchema.SetSessionModeResponse();
    }

    @Override
    public AcpSchema.SetSessionModelResponse handleSetSessionModel(Token token, AcpSchema.SetSessionModelRequest req) {
        return new AcpSchema.SetSessionModelResponse();
    }
}
```

## Minimal bean template (modify existing acpSessionHandler)

```java
@Configuration
public class AcpSessionConfiguration {

    @Bean
    public AcpSessionHandler acpSessionHandler() {
        return new AcpSessionHandler() {
            @Override
            public AcpSchema.InitializeResponse handleInit(Token token, AcpSchema.InitializeRequest req) {
                return AcpSchema.InitializeResponse.ok();
            }

            @Override
            public AcpSchema.NewSessionResponse handleNewSession(Token token, AcpSchema.NewSessionRequest req) {
                AcpSchema.SessionModeState modeState = new AcpSchema.SessionModeState("default", List.of());
                AcpSchema.SessionModelState modelState = new AcpSchema.SessionModelState("default", List.of());
                return new AcpSchema.NewSessionResponse(token.id(), modeState, modelState);
            }
        };
    }
}
```

## Verification

From repository root:

```bash
./mvnw -pl {project-name}/{project-name}-agent -DskipTests compile
```

If ACP handler tests exist:

```bash
./mvnw -pl {project-name}/{project-name}-agent test -Dtest=AcpSessionHandler*Test
```

Then verify:

- Existing `acpSessionHandler` behavior remains backward-compatible unless explicitly changed.
- `handleLoadSession` blocks mismatched user/session.
- Mode/model switch handlers validate supported values.
- If persistence is enabled, restart can restore session state.
