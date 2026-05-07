package io.github.siyukio.samples.config.api;

import io.github.siyukio.samples.TestApplication;
import io.github.siyukio.samples.config.api.dto.AdminVariableCreateRequest;
import io.github.siyukio.samples.config.api.dto.AdminVariableCreateResponse;
import io.github.siyukio.samples.config.api.dto.AdminVariableFilter;
import io.github.siyukio.samples.config.api.dto.AdminVariableGetRequest;
import io.github.siyukio.samples.config.api.dto.AdminVariableGetResponse;
import io.github.siyukio.samples.config.api.dto.AdminVariableListResponse;
import io.github.siyukio.samples.config.api.dto.AdminVariableUpdateRequest;
import io.github.siyukio.samples.config.api.dto.AdminVariableUpdateResponse;
import io.github.siyukio.samples.config.api.dto.AdminVariableRemoveRequest;
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
class AdminVariableControllerTest {

    @Autowired
    private AdminVariableController adminVariableController;

    @Test
    void createGetListUpdateAndRemoveShouldSucceed() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String key = "key-" + suffix;

        AdminVariableCreateResponse created = this.adminVariableController.create(
                new AdminVariableCreateRequest(
                        "system",
                        "description-" + suffix,
                        key,
                        "value-" + suffix
                )
        );

        assertNotNull(created.id());
        assertEquals("system", created.category());
        assertEquals(key, created.key());

        AdminVariableGetResponse loaded = this.adminVariableController.get(
                new AdminVariableGetRequest(created.id())
        );
        assertEquals(created.id(), loaded.id());
        assertEquals("value-" + suffix, loaded.value());

        PageResponse<AdminVariableListResponse> page = this.adminVariableController.list(
                new PageRequest<>(1, 20, new AdminVariableFilter("system", key))
        );
        assertNotNull(page);
        assertTrue(page.total() >= 1);
        assertNotNull(page.items());

        AdminVariableUpdateResponse updated = this.adminVariableController.update(
                new AdminVariableUpdateRequest(
                        created.id(),
                        "system",
                        "updated-" + suffix,
                        key,
                        "value-updated-" + suffix
                )
        );
        assertEquals("updated-" + suffix, updated.description());
        assertEquals("value-updated-" + suffix, updated.value());

        this.adminVariableController.remove(new AdminVariableRemoveRequest(created.id()));

        assertThrows(
                ApiException.class,
                () -> this.adminVariableController.get(new AdminVariableGetRequest(created.id()))
        );
    }

    @Test
    void createShouldRejectDuplicateCategoryAndKey() {
        String suffix = String.valueOf(System.nanoTime());
        String key = "dup-key-" + suffix;
        AdminVariableCreateRequest request = new AdminVariableCreateRequest(
                "system",
                "duplicate",
                key,
                "value-1"
        );
        this.adminVariableController.create(request);
        assertThrows(
                ApiException.class,
                () -> this.adminVariableController.create(
                        new AdminVariableCreateRequest(
                                request.category(),
                                "duplicate-2",
                                request.key(),
                                "value-2"
                        )
                )
        );
    }
}
