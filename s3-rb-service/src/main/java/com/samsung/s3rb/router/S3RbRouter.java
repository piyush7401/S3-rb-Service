package com.samsung.s3rb.router;

import com.samsung.s3rb.handler.S3RbHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class S3RbRouter {
    @Bean
    public RouterFunction<ServerResponse> s3RbRoutes(S3RbHandler handler) {
        return route(GET("/ping"), handler::ping)
                .andRoute(GET("/provider").and(accept(APPLICATION_JSON)), handler::getProvider)
                .andRoute(
                        GET("/api/config/cache-key")
                                .and(accept(APPLICATION_JSON)),
                        handler::getCacheKey
                );
    }
}
