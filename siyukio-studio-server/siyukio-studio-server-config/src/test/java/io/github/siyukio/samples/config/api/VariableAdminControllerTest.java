package io.github.siyukio.samples.config.api;

import io.github.siyukio.samples.TestApplication;
import io.github.siyukio.samples.config.api.dto.VariableAdminCreateRequest;
import io.github.siyukio.samples.config.api.dto.VariableAdminCreateResponse;
import io.github.siyukio.samples.config.api.dto.VariableAdminFilter;
import io.github.siyukio.samples.config.api.dto.VariableAdminGetRequest;
import io.github.siyukio.samples.config.api.dto.VariableAdminGetResponse;
import io.github.siyukio.samples.config.api.dto.VariableAdminListResponse;
import io.github.siyukio.samples.config.api.dto.VariableAdminUpdateRequest;
import io.github.siyukio.samples.config.api.dto.VariableAdminUpdateResponse;
import io.github.siyukio.samples.config.api.dto.VariableAdminRemoveRequest;
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

        VariableAdminCreateResponse created = this.variableAdminController.create(
                new VariableAdminCreateRequest(
                        "system",
                        "description-" + suffix,
                        key,
                        "value-" + suffix
                )
        );

        assertNotNull(created.id());
        assertEquals("system", created.category());
        assertEquals(key, created.key());

        VariableAdminGetResponse loaded = this.variableAdminController.get(
                new VariableAdminGetRequest(created.id())
        );
        assertEquals(created.id(), loaded.id());
        assertEquals("value-" + suffix, loaded.value());

        PageResponse<VariableAdminListResponse> page = this.variableAdminController.list(
                new PageRequest<>(1, 20, new VariableAdminFilter("system", key))
        );
        assertNotNull(page);
        assertTrue(page.total() >= 1);
        assertNotNull(page.items());

        VariableAdminUpdateResponse updated = this.variableAdminController.update(
                new VariableAdminUpdateRequest(
                        created.id(),
                        "system",
                        "updated-" + suffix,
                        key,
                        "value-updated-" + suffix
                )
        );
        assertEquals("updated-" + suffix, updated.description());
        assertEquals("value-updated-" + suffix, updated.value());

        this.variableAdminController.remove(new VariableAdminRemoveRequest(created.id()));

        assertThrows(
                ApiException.class,
                () -> this.variableAdminController.get(new VariableAdminGetRequest(created.id()))
        );
    }

    @Test
    void createShouldRejectDuplicateCategoryAndKey() {
        String suffix = String.valueOf(System.nanoTime());
        String key = "dup-key-" + suffix;
        VariableAdminCreateRequest request = new VariableAdminCreateRequest(
                "system",
                "duplicate",
                key,
                "value-1"
        );
        this.variableAdminController.create(request);
        assertThrows(
                ApiException.class,
                () -> this.variableAdminController.create(
                        new VariableAdminCreateRequest(
                                request.category(),
                                "duplicate-2",
                                request.key(),
                                "value-2"
                        )
                )
        );
    }
}
