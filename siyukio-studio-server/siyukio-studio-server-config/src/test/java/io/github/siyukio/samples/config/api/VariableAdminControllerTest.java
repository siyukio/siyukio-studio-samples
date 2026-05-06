package io.github.siyukio.samples.config.api;

import io.github.siyukio.samples.TestApplication;
import io.github.siyukio.samples.config.api.dto.VariableAdminFilter;
import io.github.siyukio.samples.config.api.dto.VariableAdminRequest;
import io.github.siyukio.samples.config.api.dto.VariableAdminResponse;
import io.github.siyukio.tools.api.ApiException;
import io.github.siyukio.tools.api.dto.PageRequest;
import io.github.siyukio.tools.api.dto.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("local")
class VariableAdminControllerTest {

    @Autowired
    private VariableAdminController variableAdminController;

    @Test
    void createGetListUpdateAndRemoveShouldSucceed() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String key = "key-" + suffix;

        VariableAdminResponse created = this.variableAdminController.create(
                new VariableAdminRequest(
                        null,
                        "system",
                        "description-" + suffix,
                        key,
                        "value-" + suffix
                )
        );

        assertNotNull(created.id());
        assertEquals("system", created.category());
        assertEquals(key, created.key());

        VariableAdminResponse loaded = this.variableAdminController.get(
                new VariableAdminRequest(created.id(), null, null, null, null)
        );
        assertEquals(created.id(), loaded.id());
        assertEquals("value-" + suffix, loaded.value());

        PageResponse<VariableAdminResponse> page = this.variableAdminController.list(
                new PageRequest<>(1, 20, new VariableAdminFilter("system", key))
        );
        assertNotNull(page);
        assertTrue(page.total() >= 1);
        assertNotNull(page.items());

        VariableAdminResponse updated = this.variableAdminController.update(
                new VariableAdminRequest(
                        created.id(),
                        "system",
                        "updated-" + suffix,
                        key,
                        "value-updated-" + suffix
                )
        );
        assertEquals("updated-" + suffix, updated.description());
        assertEquals("value-updated-" + suffix, updated.value());

        this.variableAdminController.remove(new VariableAdminRequest(created.id(), null, null, null, null));

        assertThrows(
                ApiException.class,
                () -> this.variableAdminController.get(new VariableAdminRequest(created.id(), null, null, null, null))
        );
    }

    @Test
    void createShouldRejectDuplicateCategoryAndKey() {
        String suffix = String.valueOf(System.nanoTime());
        String key = "dup-key-" + suffix;
        VariableAdminRequest request = new VariableAdminRequest(
                null,
                "system",
                "duplicate",
                key,
                "value-1"
        );
        this.variableAdminController.create(request);
        assertThrows(
                ApiException.class,
                () -> this.variableAdminController.create(
                        new VariableAdminRequest(
                                null,
                                request.category(),
                                "duplicate-2",
                                request.key(),
                                "value-2"
                        )
                )
        );
    }
}
