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
import io.github.siyukio.samples.config.model.entity.Variable;
import io.github.siyukio.tools.api.ApiException;
import io.github.siyukio.tools.api.dto.PageRequest;
import io.github.siyukio.tools.api.dto.PageResponse;
import io.github.siyukio.tools.entity.postgresql.PgEntityDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("local")
class AdminVariableControllerTest {

    @Autowired
    private AdminVariableController adminVariableController;

    @Autowired
    private PgEntityDao<Variable> variablePgEntityDao;

    @Test
    void createGetListUpdateAndFilterByEnabledShouldSucceed() {
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
        assertTrue(created.enabled());

        AdminVariableGetResponse loaded = this.adminVariableController.get(
                new AdminVariableGetRequest(created.id())
        );
        assertEquals(created.id(), loaded.id());
        assertEquals("value-" + suffix, loaded.value());
        assertTrue(loaded.enabled());

        PageResponse<AdminVariableListResponse> page = this.adminVariableController.list(
                new PageRequest<>(1, 20, new AdminVariableFilter("system", key, true))
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
                        "value-updated-" + suffix,
                        false
                )
        );
        assertEquals("updated-" + suffix, updated.description());
        assertEquals("value-updated-" + suffix, updated.value());
        assertFalse(updated.enabled());

        AdminVariableGetResponse disabled = this.adminVariableController.get(
                new AdminVariableGetRequest(created.id())
        );
        assertEquals(created.id(), disabled.id());
        assertFalse(disabled.enabled());

        PageResponse<AdminVariableListResponse> disabledPage = this.adminVariableController.list(
                new PageRequest<>(1, 20, new AdminVariableFilter("system", key, false))
        );
        assertNotNull(disabledPage);
        assertEquals(1, disabledPage.total());
        assertEquals(created.id(), disabledPage.items().getFirst().id());

        PageResponse<AdminVariableListResponse> enabledPage = this.adminVariableController.list(
                new PageRequest<>(1, 20, new AdminVariableFilter("system", key, true))
        );
        assertNotNull(enabledPage);
        assertEquals(0, enabledPage.total());

        Variable updatedEntity = this.variablePgEntityDao.queryById(created.id());
        assertNotNull(updatedEntity);
        assertFalse(updatedEntity.enabled());
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

    @Test
    void createShouldRejectDuplicateCategoryAndKeyWhenExistingVariableDisabled() {
        String suffix = String.valueOf(System.nanoTime());
        String key = "disabled-dup-key-" + suffix;
        AdminVariableCreateResponse created = this.adminVariableController.create(
                new AdminVariableCreateRequest(
                        "system",
                        "before-disable",
                        key,
                        "before-disable"
                )
        );

        AdminVariableUpdateResponse disabled = this.adminVariableController.update(
                new AdminVariableUpdateRequest(
                        created.id(),
                        null,
                        null,
                        null,
                        null,
                        false
                )
        );
        assertFalse(disabled.enabled());

        assertThrows(
                ApiException.class,
                () -> this.adminVariableController.create(
                        new AdminVariableCreateRequest(
                                "system",
                                "duplicate-after-disable",
                                key,
                                "duplicate-after-disable"
                        )
                )
        );
    }
}
