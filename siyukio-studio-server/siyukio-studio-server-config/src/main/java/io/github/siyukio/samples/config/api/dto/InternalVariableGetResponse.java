package io.github.siyukio.samples.config.api.dto;

import io.github.siyukio.tools.api.annotation.ApiParameter;

public record InternalVariableGetResponse(

        @ApiParameter(description = "Variable category")
        String category,

        @ApiParameter(description = "Variable description")
        String description,

        @ApiParameter(description = "Variable key")
        String key,

        @ApiParameter(description = "Variable value", password = true)
        String value,

        @ApiParameter(description = "Variable enabled status")
        boolean enabled

) {
}
