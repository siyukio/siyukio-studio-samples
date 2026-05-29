package io.github.siyukio.samples.config.api;

import io.github.siyukio.samples.config.api.dto.InternalVariableGetRequest;
import io.github.siyukio.samples.config.api.dto.InternalVariableGetResponse;
import io.github.siyukio.samples.config.api.paths.InternalVariablePaths;
import io.github.siyukio.samples.config.application.VariableService;
import io.github.siyukio.tools.api.annotation.ApiController;
import io.github.siyukio.tools.api.annotation.ApiMapping;
import io.github.siyukio.tools.api.annotation.Authorization;
import io.github.siyukio.tools.api.token.Token;
import org.springframework.beans.factory.annotation.Autowired;

@ApiController(
        summary = "Variable internal API",
        authorization = @Authorization(type = Token.PRINCIPAL_TYPE_INTERNAL)
)
public class InternalVariableController {

    @Autowired
    private VariableService variableService;

    @ApiMapping(path = InternalVariablePaths.GET, summary = "Get variable by category and key")
    public InternalVariableGetResponse get(
            InternalVariableGetRequest request
    ) {
        return this.variableService.getInternalVariable(request);
    }
}
