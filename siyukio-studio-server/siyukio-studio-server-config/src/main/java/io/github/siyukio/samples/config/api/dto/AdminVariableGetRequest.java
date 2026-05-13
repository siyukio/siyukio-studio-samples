package io.github.siyukio.samples.config.api.dto;

import io.github.siyukio.tools.api.annotation.ApiParameter;

public record AdminVariableGetRequest(

        @ApiParameter(description = "Variable id", required = true)
        String id

) {
}
