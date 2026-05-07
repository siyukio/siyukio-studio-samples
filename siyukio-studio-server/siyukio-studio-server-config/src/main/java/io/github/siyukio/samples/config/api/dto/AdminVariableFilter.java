package io.github.siyukio.samples.config.api.dto;

import io.github.siyukio.tools.api.annotation.ApiParameter;

public record AdminVariableFilter(

        @ApiParameter(description = "Variable category", required = false)
        String category,

        @ApiParameter(description = "Variable key", required = false)
        String key

) {
}
