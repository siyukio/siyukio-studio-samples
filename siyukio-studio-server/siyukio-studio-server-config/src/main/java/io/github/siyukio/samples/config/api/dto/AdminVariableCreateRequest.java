package io.github.siyukio.samples.config.api.dto;

import io.github.siyukio.tools.api.annotation.ApiParameter;

public record AdminVariableCreateRequest(

        @ApiParameter(description = "Variable category", required = true)
        String category,

        @ApiParameter(description = "Variable description", required = false)
        String description,

        @ApiParameter(description = "Variable key", required = true)
        String key,

        @ApiParameter(description = "Variable value", required = true)
        String value

) {
}
