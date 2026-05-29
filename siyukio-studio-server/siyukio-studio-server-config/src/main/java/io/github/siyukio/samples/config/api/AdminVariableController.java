package io.github.siyukio.samples.config.api;

import io.github.siyukio.samples.config.api.dto.AdminVariableCreateRequest;
import io.github.siyukio.samples.config.api.dto.AdminVariableCreateResponse;
import io.github.siyukio.samples.config.api.dto.AdminVariableFilter;
import io.github.siyukio.samples.config.api.dto.AdminVariableGetRequest;
import io.github.siyukio.samples.config.api.dto.AdminVariableGetResponse;
import io.github.siyukio.samples.config.api.dto.AdminVariableListResponse;
import io.github.siyukio.samples.config.api.dto.AdminVariableUpdateRequest;
import io.github.siyukio.samples.config.api.dto.AdminVariableUpdateResponse;
import io.github.siyukio.samples.config.api.paths.AdminVariablePaths;
import io.github.siyukio.samples.config.application.VariableService;
import io.github.siyukio.tools.api.annotation.ApiController;
import io.github.siyukio.tools.api.annotation.ApiMapping;
import io.github.siyukio.tools.api.annotation.Authorization;
import io.github.siyukio.tools.api.dto.PageRequest;
import io.github.siyukio.tools.api.dto.PageResponse;
import io.github.siyukio.tools.api.token.Token;
import org.springframework.beans.factory.annotation.Autowired;

@ApiController(
        summary = "Variable admin API",
        authorization = @Authorization(type = Token.PRINCIPAL_TYPE_ADMIN_USER)
)
public class AdminVariableController {

    @Autowired
    private VariableService variableService;

    @ApiMapping(path = AdminVariablePaths.LIST, summary = "Query variable list")
    public PageResponse<AdminVariableListResponse> list(
            PageRequest<AdminVariableFilter> request
    ) {
        return this.variableService.queryVariablePage(request);
    }

    @ApiMapping(path = AdminVariablePaths.CREATE, summary = "Create variable")
    public AdminVariableCreateResponse create(
            AdminVariableCreateRequest request
    ) {
        return this.variableService.createVariable(request);
    }

    @ApiMapping(path = AdminVariablePaths.GET, summary = "Get variable by id")
    public AdminVariableGetResponse get(
            AdminVariableGetRequest request
    ) {
        return this.variableService.getVariable(request);
    }

    @ApiMapping(path = AdminVariablePaths.UPDATE, summary = "Update variable by id")
    public AdminVariableUpdateResponse update(
            AdminVariableUpdateRequest request
    ) {
        return this.variableService.updateVariable(request);
    }
}
