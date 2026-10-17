package com.samsung.s3rb.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class S3RbHandler {
    public Mono<ServerResponse> ping(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
                .bodyValue("S3 RB Service is running");
    }

    public Mono<ServerResponse> getProvider(ServerRequest request) {
        String previewValue = request.queryParam("preview").orElse("false");
        if (!previewValue.equalsIgnoreCase("true") && !previewValue.equalsIgnoreCase("false")) {
            return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("error", "preview must be true or false"));
        }
        boolean preview = Boolean.parseBoolean(previewValue);
        Map<String, Object> response = preview
                ? Map.of("source", "S3/Athena (mock)", "preview", true,
                         "providers", new String[]{"Provider-A", "Provider-B"})
                : Map.of("source", "Redis (mock)", "preview", false,
                         "providers", new String[]{"Provider-A"});
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(response);
    }
}
