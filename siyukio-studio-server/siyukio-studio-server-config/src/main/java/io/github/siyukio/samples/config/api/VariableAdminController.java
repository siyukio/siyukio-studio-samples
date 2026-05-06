package io.github.siyukio.samples.config.api;

import io.github.siyukio.samples.common.constants.RolesConstants;
import io.github.siyukio.samples.config.api.dto.VariableAdminFilter;
import io.github.siyukio.samples.config.api.dto.VariableAdminRequest;
import io.github.siyukio.samples.config.api.dto.VariableAdminResponse;
import io.github.siyukio.samples.config.api.paths.VariableAdminPaths;
import io.github.siyukio.samples.config.application.VariableService;
import io.github.siyukio.tools.api.annotation.ApiController;
import io.github.siyukio.tools.api.annotation.ApiMapping;
import io.github.siyukio.tools.api.dto.PageRequest;
import io.github.siyukio.tools.api.dto.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;

@ApiController(
        summary = "Variable admin API",
        roles = {RolesConstants.ADMIN}
)
public class VariableAdminController {

    @Autowired
    private VariableService variableService;

    @ApiMapping(path = VariableAdminPaths.LIST, summary = "Query variable list")
    public PageResponse<VariableAdminResponse> list(
            PageRequest<VariableAdminFilter> request
    ) {
        return this.variableService.queryVariablePage(request);
    }

    @ApiMapping(path = VariableAdminPaths.CREATE, summary = "Create variable")
    public VariableAdminResponse create(
            VariableAdminRequest request
    ) {
        return this.variableService.createVariable(request);
    }

    @ApiMapping(path = VariableAdminPaths.GET, summary = "Get variable by id")
    public VariableAdminResponse get(
            VariableAdminRequest request
    ) {
        return this.variableService.getVariable(request);
    }

    @ApiMapping(path = VariableAdminPaths.UPDATE, summary = "Update variable by id")
    public VariableAdminResponse update(
            VariableAdminRequest request
    ) {
        return this.variableService.updateVariable(request);
    }

    @ApiMapping(path = VariableAdminPaths.REMOVE, summary = "Remove variable by id")
    public void remove(
            VariableAdminRequest request
    ) {
        this.variableService.removeVariable(request);
    }
}
