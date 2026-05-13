package io.github.siyukio.samples.config.client.dto;

import io.github.siyukio.tools.api.annotation.ApiParameter;

public record InternalVariableGetRequest(

        @ApiParameter(description = "Variable category, default is default", required = false)
        String category,

        @ApiParameter(description = "Variable key", required = true)
        String key

) {
}
