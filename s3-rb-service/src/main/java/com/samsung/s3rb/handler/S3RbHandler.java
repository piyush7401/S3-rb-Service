package com.samsung.s3rb.handler;

import com.samsung.s3rb.service.ProviderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class S3RbHandler {

    private final ProviderService providerService;

    public S3RbHandler(ProviderService providerService) {
        this.providerService = providerService;
    }

    public Mono<ServerResponse> ping(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("S3 RB Service is running");
    }

    public Mono<ServerResponse> getProvider(ServerRequest request) {

        String previewValue =
                request.queryParam("preview").orElse("false");

        if (!previewValue.equalsIgnoreCase("true")
                && !previewValue.equalsIgnoreCase("false")) {

            return ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "error", "preview must be true or false"
                    ));
        }
        boolean preview = Boolean.parseBoolean(previewValue);

        return providerService.getProvider(preview)
                .flatMap(response ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response)
                );
    }
}
