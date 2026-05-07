package io.github.siyukio.samples.config.api.dto;

import io.github.siyukio.tools.api.annotation.ApiParameter;

import java.time.LocalDateTime;

public record VariableAdminCreateResponse(

        @ApiParameter(description = "Variable id")
        String id,

        @ApiParameter(description = "Variable category")
        String category,

        @ApiParameter(description = "Variable description")
        String description,

        @ApiParameter(description = "Variable key")
        String key,

        @ApiParameter(description = "Variable value", password = true)
        String value,

        @ApiParameter(description = "Created at")
        LocalDateTime createdAt,

        @ApiParameter(description = "Created at timestamp")
        long createdAtTs,

        @ApiParameter(description = "Updated at")
        LocalDateTime updatedAt,

        @ApiParameter(description = "Updated at timestamp")
        long updatedAtTs

) {
}

