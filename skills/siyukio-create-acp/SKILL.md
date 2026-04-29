---
name: siyukio-create-acp
description: Generate ACP (Agent Client Protocol) session handler code for Siyukio-based Spring Boot applications. Use when adding a new agent session lifecycle, session model/mode state management, cancellation handling, or ACP tool-call progress/result signaling over WebSocket.
---

# Goal

Create `AcpSessionHandlerImpl` for Siyukio ACP modules, with correct session ownership checks and explicit model/mode state handling.

## Required inputs

- `{Agent}`: class-level business name in PascalCase (for comments/log context).
- Supported mode values (for example: `default`, `code`, `review`).
- Supported model values (for example: `gpt-4.1`, `gpt-5`).
- Whether session state is in-memory only or persisted.

## Prerequisites

- Use server modules only.
- Ensure target module exists: `{project-name}/{project-name}-domain-agent/`.
- Ensure dependency exists in `{project-name}-domain-agent/pom.xml`:

```xml
<dependency>
  <groupId>io.github.siyukio</groupId>
  <artifactId>spring-siyukio-application-acp</artifactId>
</dependency>
```

## Files to create or update

- `{project-name}/{project-name}-domain-agent/src/main/java/{package-path}/agent/application/acp/AcpSessionHandlerImpl.java`
- Optional: persistence service/repository classes if durable session state is required.

## Implementation protocol

1. Implement `AcpSessionHandlerImpl` in package `{package-name}.agent.application.acp`.
2. Annotate class with `@Service` and `@Slf4j`.
3. Accept `Token` as the first parameter in every handler method.
4. In `handleNewSession`, initialize model/mode states and return `token.id()` as session ID.
5. In `handleLoadSession`, reject unknown or cross-user sessions using protocol exception.
6. In `handleSetSessionMode` and `handleSetSessionModel`, validate requested value, then update state.
7. In `handleCancel`, stop in-flight operations and clean temporary resources.
8. If persistence is enabled, restore and save session state via domain service/repository.

## Minimal template

```java
package {package-name}.agent.application.acp;

import com.agentclientprotocol.sdk.error.AcpProtocolException;
import com.agentclientprotocol.sdk.spec.AcpSchema;
import io.github.siyukio.tools.api.token.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AcpSessionHandlerImpl implements AcpSessionHandler {

    @Override
    public AcpSchema.NewSessionResponse handleNewSession(Token token, AcpSchema.NewSessionRequest req) {
        AcpSchema.SessionModeState modeState = new AcpSchema.SessionModeState("default", List.of("default"));
        AcpSchema.SessionModelState modelState = new AcpSchema.SessionModelState("default", List.of("chat"));
        return new AcpSchema.NewSessionResponse(token.id(), modeState, modelState);
    }

    @Override
    public AcpSchema.LoadSessionResponse handleLoadSession(Token token, AcpSchema.LoadSessionRequest req) {
        if (!token.id().equals(req.sessionId())) {
            throw new AcpProtocolException(HttpStatus.NOT_FOUND.value(), "Session not found: " + req.sessionId());
        }
        AcpSchema.SessionModeState modeState = new AcpSchema.SessionModeState("default", List.of("default"));
        AcpSchema.SessionModelState modelState = new AcpSchema.SessionModelState("default", List.of("chat"));
        return new AcpSchema.LoadSessionResponse(modeState, modelState);
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

## Validation checklist

1. Run `./mvnw compile`.
2. Ensure all `AcpSessionHandler` interface methods are implemented.
3. Verify `handleLoadSession` denies mismatched user/session.
4. If persistence is enabled, verify restore-after-restart behavior.
