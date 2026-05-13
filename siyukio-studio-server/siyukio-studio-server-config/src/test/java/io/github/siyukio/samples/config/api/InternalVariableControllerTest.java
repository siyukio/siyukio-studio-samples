package io.github.siyukio.samples.config.api;

import io.github.siyukio.samples.TestApplication;
import io.github.siyukio.samples.config.api.dto.AdminVariableCreateRequest;
import io.github.siyukio.samples.config.api.dto.InternalVariableGetRequest;
import io.github.siyukio.samples.config.api.dto.InternalVariableGetResponse;
import io.github.siyukio.tools.api.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("local")
class InternalVariableControllerTest {

    @Autowired
    private AdminVariableController adminVariableController;

    @Autowired
    private InternalVariableController internalVariableController;

    @Test
    void getShouldUseDefaultCategoryWhenCategoryIsBlank() {
        String suffix = String.valueOf(System.nanoTime());
        String key = "internal-default-key-" + suffix;

        this.adminVariableController.create(
                new AdminVariableCreateRequest(
                        "default",
                        "default config",
                        key,
                        "default-value-" + suffix
                )
        );
        this.adminVariableController.create(
                new AdminVariableCreateRequest(
                        "system",
                        "system config",
                        key,
                        "system-value-" + suffix
                )
        );

        InternalVariableGetResponse response = this.internalVariableController.get(
                new InternalVariableGetRequest(null, key)
        );

        assertEquals("default", response.category());
        assertEquals(key, response.key());
        assertEquals("default-value-" + suffix, response.value());
    }

    @Test
    void getShouldUseRequestedCategoryWhenProvided() {
        String suffix = String.valueOf(System.nanoTime());
        String key = "internal-system-key-" + suffix;

        this.adminVariableController.create(
                new AdminVariableCreateRequest(
                        "default",
                        "default config",
                        key,
                        "default-value-" + suffix
                )
        );
        this.adminVariableController.create(
                new AdminVariableCreateRequest(
                        "system",
                        "system config",
                        key,
                        "system-value-" + suffix
                )
        );

        InternalVariableGetResponse response = this.internalVariableController.get(
                new InternalVariableGetRequest("system", key)
        );

        assertEquals("system", response.category());
        assertEquals(key, response.key());
        assertEquals("system-value-" + suffix, response.value());
    }

    @Test
    void getShouldRejectBlankKey() {
        assertThrows(
                ApiException.class,
                () -> this.internalVariableController.get(
                        new InternalVariableGetRequest("default", " ")
                )
        );
    }
}
