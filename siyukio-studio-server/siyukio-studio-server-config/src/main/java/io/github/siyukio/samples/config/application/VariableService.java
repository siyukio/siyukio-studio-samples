package io.github.siyukio.samples.config.application;

import io.github.siyukio.samples.config.api.dto.AdminVariableCreateRequest;
import io.github.siyukio.samples.config.api.dto.AdminVariableCreateResponse;
import io.github.siyukio.samples.config.api.dto.AdminVariableFilter;
import io.github.siyukio.samples.config.api.dto.AdminVariableGetRequest;
import io.github.siyukio.samples.config.api.dto.AdminVariableGetResponse;
import io.github.siyukio.samples.config.api.dto.AdminVariableListResponse;
import io.github.siyukio.samples.config.api.dto.AdminVariableUpdateRequest;
import io.github.siyukio.samples.config.api.dto.AdminVariableUpdateResponse;
import io.github.siyukio.samples.config.model.entity.Variable;
import io.github.siyukio.samples.config.model.errors.VariableErrors;
import io.github.siyukio.samples.config.model.policy.VariablePolicy;
import io.github.siyukio.tools.api.ApiException;
import io.github.siyukio.tools.api.dto.PageRequest;
import io.github.siyukio.tools.api.dto.PageResponse;
import io.github.siyukio.tools.entity.page.Page;
import io.github.siyukio.tools.entity.postgresql.PgEntityDao;
import io.github.siyukio.tools.entity.query.BoolQueryBuilder;
import io.github.siyukio.tools.entity.query.QueryBuilder;
import io.github.siyukio.tools.entity.query.QueryBuilders;
import io.github.siyukio.tools.entity.sort.SortBuilders;
import io.github.siyukio.tools.entity.sort.SortOrder;
import io.github.siyukio.tools.util.XDataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class VariableService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 1000;

    @Autowired
    private PgEntityDao<Variable> variablePgEntityDao;

    @Autowired
    private VariablePolicy variablePolicy;

    public PageResponse<AdminVariableListResponse> queryVariablePage(PageRequest<AdminVariableFilter> request) {
        if (request == null) {
            request = PageRequest.<AdminVariableFilter>builder()
                    .page(DEFAULT_PAGE)
                    .size(DEFAULT_PAGE_SIZE)
                    .build();
        }
        QueryBuilder queryBuilder = this.buildFilterQuery(request.filter());
        Page<Variable> page = this.variablePgEntityDao.queryPage(
                queryBuilder,
                SortBuilders.fieldSort(
                        SortBuilders.fieldSort("enabled").order(SortOrder.DESC),
                        SortBuilders.fieldSort("updatedAtTs").order(SortOrder.DESC)
                ),
                this.normalizePage(request.page()),
                this.normalizeSize(request.size())
        );

        List<AdminVariableListResponse> items = page.items() == null
                ? Collections.emptyList()
                : XDataUtils.copy(page.items(), List.class, AdminVariableListResponse.class);
        return PageResponse.<AdminVariableListResponse>builder()
                .total(page.total())
                .items(items)
                .build();
    }

    @Transactional
    public AdminVariableCreateResponse createVariable(AdminVariableCreateRequest request) {
        String category = this.requireText(request.category(), VariableErrors.VARIABLE_CATEGORY_REQUIRED);
        String key = this.requireText(request.key(), VariableErrors.VARIABLE_KEY_REQUIRED);
        String value = this.requireText(request.value(), VariableErrors.VARIABLE_VALUE_REQUIRED);
        String description = this.trimToNull(request.description());

        this.variablePolicy.checkVariableUnique(category, key, null);
        Variable created = this.variablePgEntityDao.insert(Variable.builder()
                .id(null)
                .category(category)
                .description(description)
                .key(key)
                .value(value)
                .enabled(true)
                .build());
        return XDataUtils.copy(created, AdminVariableCreateResponse.class);
    }

    public AdminVariableGetResponse getVariable(AdminVariableGetRequest request) {
        String id = this.requireText(request.id(), VariableErrors.VARIABLE_ID_REQUIRED);
        Variable variable = this.variablePolicy.checkVariableExists(id);
        return XDataUtils.copy(variable, AdminVariableGetResponse.class);
    }

    @Transactional
    public AdminVariableUpdateResponse updateVariable(AdminVariableUpdateRequest request) {
        String id = this.requireText(request.id(), VariableErrors.VARIABLE_ID_REQUIRED);
        Variable current = this.variablePolicy.checkVariableExists(id);

        Variable merged = XDataUtils.mergeNotNul(request, current);

        this.variablePolicy.checkVariableUnique(merged.category(), merged.key(), id);
        Variable updated = this.variablePgEntityDao.update(merged);
        return XDataUtils.copy(updated, AdminVariableUpdateResponse.class);
    }

    private QueryBuilder buildFilterQuery(AdminVariableFilter filter) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if (filter == null) {
            return queryBuilder;
        }
        if (StringUtils.hasText(filter.category())) {
            queryBuilder.must(QueryBuilders.termQuery("category", filter.category().trim()));
        }
        if (StringUtils.hasText(filter.key())) {
            queryBuilder.must(QueryBuilders.termQuery("key", filter.key().trim()));
        }
        if (filter.enabled() != null) {
            queryBuilder.must(QueryBuilders.termQuery("enabled", filter.enabled()));
        }
        return queryBuilder;
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String requireText(String value, String error) {
        if (!StringUtils.hasText(value)) {
            throw new ApiException(error);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
