package io.github.siyukio.samples.config.client;

import io.github.siyukio.samples.config.client.dto.InternalVariableGetRequest;
import io.github.siyukio.samples.config.client.dto.InternalVariableGetResponse;
import io.github.siyukio.samples.config.client.paths.InternalVariablePaths;
import io.github.siyukio.tools.api.annotation.client.ApiClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

@ApiClient(url = "${internal.url}", headers = {
        "Authorization=${internal.authorization}"
})
public interface VariableClient {

    @PostExchange(InternalVariablePaths.GET)
    InternalVariableGetResponse get(@RequestBody InternalVariableGetRequest request);
}
