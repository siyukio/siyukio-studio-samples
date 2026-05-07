package io.github.siyukio.samples.config.api.dto;

import io.github.siyukio.tools.api.annotation.ApiParameter;

public record AdminVariableUpdateRequest(

        @ApiParameter(description = "Variable id", required = true)
        String id,

        @ApiParameter(description = "Variable category", required = false)
        String category,

        @ApiParameter(description = "Variable description", required = false)
        String description,

        @ApiParameter(description = "Variable key", required = false)
        String key,

        @ApiParameter(description = "Variable value", required = false)
        String value,

        @ApiParameter(description = "Variable enabled status", required = false)
        Boolean enabled

) {
}
