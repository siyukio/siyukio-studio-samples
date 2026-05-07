package io.github.siyukio.samples.config.application;

import io.github.siyukio.samples.config.api.dto.VariableAdminCreateRequest;
import io.github.siyukio.samples.config.api.dto.VariableAdminCreateResponse;
import io.github.siyukio.samples.config.api.dto.VariableAdminFilter;
import io.github.siyukio.samples.config.api.dto.VariableAdminGetRequest;
import io.github.siyukio.samples.config.api.dto.VariableAdminGetResponse;
import io.github.siyukio.samples.config.api.dto.VariableAdminListResponse;
import io.github.siyukio.samples.config.api.dto.VariableAdminRemoveRequest;
import io.github.siyukio.samples.config.api.dto.VariableAdminUpdateRequest;
import io.github.siyukio.samples.config.api.dto.VariableAdminUpdateResponse;
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

    public PageResponse<VariableAdminListResponse> queryVariablePage(PageRequest<VariableAdminFilter> request) {
        if (request == null) {
            request = PageRequest.<VariableAdminFilter>builder()
                    .page(DEFAULT_PAGE)
                    .size(DEFAULT_PAGE_SIZE)
                    .build();
        }
        QueryBuilder queryBuilder = this.buildFilterQuery(request.filter());
        Page<Variable> page = this.variablePgEntityDao.queryPage(
                queryBuilder,
                SortBuilders.fieldSort("updatedAtTs").order(SortOrder.DESC),
                this.normalizePage(request.page()),
                this.normalizeSize(request.size())
        );

        List<VariableAdminListResponse> items = page.items() == null
                ? Collections.emptyList()
                : XDataUtils.copy(page.items(), List.class, VariableAdminListResponse.class);
        return PageResponse.<VariableAdminListResponse>builder()
                .total(page.total())
                .items(items)
                .build();
    }

    @Transactional
    public VariableAdminCreateResponse createVariable(VariableAdminCreateRequest request) {
        String category = this.requireText(request.category(), VariableErrors.VARIABLE_CATEGORY_REQUIRED);
        String key = this.requireText(request.key(), VariableErrors.VARIABLE_KEY_REQUIRED);
        String value = this.requireText(request.value(), VariableErrors.VARIABLE_VALUE_REQUIRED);

        this.variablePolicy.checkVariableUnique(category, key, null);
        Variable created = this.variablePgEntityDao.insert(new Variable(
                null,
                category,
                this.trimToNull(request.description()),
                key,
                value,
                null,
                null,
                0L,
                null,
                0L
        ));
        return XDataUtils.copy(created, VariableAdminCreateResponse.class);
    }

    public VariableAdminGetResponse getVariable(VariableAdminGetRequest request) {
        String id = this.requireText(request.id(), VariableErrors.VARIABLE_ID_REQUIRED);
        Variable variable = this.variablePolicy.checkVariableExists(id);
        return XDataUtils.copy(variable, VariableAdminGetResponse.class);
    }

    @Transactional
    public VariableAdminUpdateResponse updateVariable(VariableAdminUpdateRequest request) {
        String id = this.requireText(request.id(), VariableErrors.VARIABLE_ID_REQUIRED);
        Variable current = this.variablePolicy.checkVariableExists(id);

        String nextCategory = request.category() == null
                ? current.category()
                : this.requireText(request.category(), VariableErrors.VARIABLE_CATEGORY_REQUIRED);
        String nextKey = request.key() == null
                ? current.key()
                : this.requireText(request.key(), VariableErrors.VARIABLE_KEY_REQUIRED);
        String nextValue = request.value() == null
                ? current.value()
                : this.requireText(request.value(), VariableErrors.VARIABLE_VALUE_REQUIRED);
        String nextDescription = request.description() == null
                ? current.description()
                : this.trimToNull(request.description());

        this.variablePolicy.checkVariableUnique(nextCategory, nextKey, id);
        Variable updated = this.variablePgEntityDao.update(new Variable(
                current.id(),
                nextCategory,
                nextDescription,
                nextKey,
                nextValue,
                current.salt(),
                current.createdAt(),
                current.createdAtTs(),
                current.updatedAt(),
                current.updatedAtTs()
        ));
        return XDataUtils.copy(updated, VariableAdminUpdateResponse.class);
    }

    @Transactional
    public void removeVariable(VariableAdminRemoveRequest request) {
        String id = this.requireText(request.id(), VariableErrors.VARIABLE_ID_REQUIRED);
        this.variablePolicy.checkVariableExists(id);
        this.variablePgEntityDao.deleteById(id);
    }

    private QueryBuilder buildFilterQuery(VariableAdminFilter filter) {
        if (filter == null) {
            return null;
        }
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        boolean hasQuery = false;
        if (StringUtils.hasText(filter.category())) {
            queryBuilder.must(QueryBuilders.termQuery("category", filter.category().trim()));
            hasQuery = true;
        }
        if (StringUtils.hasText(filter.key())) {
            queryBuilder.must(QueryBuilders.termQuery("key", filter.key().trim()));
            hasQuery = true;
        }
        return hasQuery ? queryBuilder : null;
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
