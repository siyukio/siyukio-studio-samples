package io.github.siyukio.samples.config.model.errors;

public interface VariableErrors {

    String VARIABLE_ID_REQUIRED = "Variable id is required";

    String VARIABLE_CATEGORY_REQUIRED = "Variable category is required";

    String VARIABLE_KEY_REQUIRED = "Variable key is required";

    String VARIABLE_VALUE_REQUIRED = "Variable value is required";

    String VARIABLE_NOT_FOUND = "Variable not found: %s";

    String VARIABLE_NOT_FOUND_BY_CATEGORY_AND_KEY = "Variable not found: category=%s, key=%s";

    String VARIABLE_ALREADY_EXISTS = "Variable already exists: category=%s, key=%s";
}
