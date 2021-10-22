package com.dynamodb.webflux.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;

import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
    public RouterFunction<ServerResponse> personRouter(PersonHandler handler) {
        return route(GET("/api/person/{id}").and(accept(APPLICATION_JSON)), handler::getPerson)
                .andRoute(POST("/api/person").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::create)
                .andRoute(PUT("/api/person/{id}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::edit)
                .andRoute(DELETE("/api/person/{id}"), handler::delete);
    }

}
