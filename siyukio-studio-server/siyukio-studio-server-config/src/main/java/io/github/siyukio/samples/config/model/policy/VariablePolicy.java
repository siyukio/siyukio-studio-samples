package io.github.siyukio.samples.config.model.policy;

import io.github.siyukio.samples.config.model.entity.Variable;
import io.github.siyukio.samples.config.model.errors.VariableErrors;
import io.github.siyukio.tools.api.ApiException;
import io.github.siyukio.tools.entity.postgresql.PgEntityDao;
import io.github.siyukio.tools.entity.query.BoolQueryBuilder;
import io.github.siyukio.tools.entity.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class VariablePolicy {

    @Autowired
    private PgEntityDao<Variable> variablePgEntityDao;

    public Variable checkVariableExists(String id) {
        if (!StringUtils.hasText(id)) {
            throw new ApiException(VariableErrors.VARIABLE_ID_REQUIRED);
        }
        Variable variable = this.variablePgEntityDao.queryById(id.trim());
        if (variable == null) {
            throw new ApiException(String.format(VariableErrors.VARIABLE_NOT_FOUND, id));
        }
        return variable;
    }

    public void checkVariableUnique(String category, String key, String excludeId) {
        if (!StringUtils.hasText(category)) {
            throw new ApiException(VariableErrors.VARIABLE_CATEGORY_REQUIRED);
        }
        if (!StringUtils.hasText(key)) {
            throw new ApiException(VariableErrors.VARIABLE_KEY_REQUIRED);
        }
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("category", category.trim()))
                .must(QueryBuilders.termQuery("key", key.trim()));
        if (StringUtils.hasText(excludeId)) {
            queryBuilder.mustNot(QueryBuilders.termQuery("id", excludeId.trim()));
        }
        Variable exists = this.variablePgEntityDao.queryOne(queryBuilder);
        if (exists != null) {
            throw new ApiException(String.format(
                    VariableErrors.VARIABLE_ALREADY_EXISTS,
                    category,
                    key
            ));
        }
    }
}
