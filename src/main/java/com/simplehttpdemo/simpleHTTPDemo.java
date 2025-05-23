package com.simplehttpdemo;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import java.util.Optional;

// OTel Imports
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.DoubleCounter;

/**
 * Azure Functions with HTTP Trigger.
 */
public class simpleHTTPDemo {    

    // OTel Counter stuff...
    private final String INSTRUMENTATION_SCOPE = "com.simplehttpdemo";
    private final AttributeKey<String> NAME_KEY = stringKey("user.name");
    private Meter meter;

    public simpleHTTPDemo() {
        this.meter = GlobalOpenTelemetry.getMeter(INSTRUMENTATION_SCOPE);
    }

    DoubleCounter createCounter() {
        return meter
            .counterBuilder("my.test.counter")
            .setDescription("A count of names")
            .setUnit("unit")
            .ofDoubles()
            .build();
    }

    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        final String query = request.getQueryParameters().get("name");
        final String name = request.getBody().orElse(query);

        DoubleCounter nameCounter = createCounter();
        context.getLogger().info("Counter created: " + nameCounter.toString());

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            nameCounter.add(1, Attributes.of(NAME_KEY, name));
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
        }
    }
}
