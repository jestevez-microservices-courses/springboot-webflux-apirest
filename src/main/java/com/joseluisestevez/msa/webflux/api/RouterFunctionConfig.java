package com.joseluisestevez.msa.webflux.api;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.joseluisestevez.msa.webflux.api.handler.ProductHandler;

@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler productHandler) {
        return route(GET("/api/v2/products").or(GET("/api/v3/products")), productHandler::list)
                .andRoute(GET("/api/v2/products/{id}"), productHandler::view).andRoute(POST("/api/v2/products"), productHandler::create)
                .andRoute(PUT("/api/v2/products/{id}"), productHandler::edit).andRoute(DELETE("/api/v2/products/{id}"), productHandler::delete);
    }
}
