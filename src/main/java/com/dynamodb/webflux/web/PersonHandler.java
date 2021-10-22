package com.dynamodb.webflux.web;

import com.dynamodb.webflux.service.DynamoDbService;
import com.dynamodb.webflux.domain.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.util.concurrent.CompletableFuture;


@Component
public class PersonHandler {

    @Autowired
    private DynamoDbService dynamoDbService;

    Mono<ServerResponse> getPerson(ServerRequest request) {
        String eventId = request.pathVariable("id");
        CompletableFuture<Person> eventGetFuture = dynamoDbService.getPerson(eventId);
        Mono<Person> eventMono = Mono.fromFuture(eventGetFuture);
        return ServerResponse.ok().body(eventMono, Person.class);
    }

    Mono<ServerResponse> create(ServerRequest request) {
        Mono<Person> eventMono = request.bodyToMono(Person.class);

        return eventMono.flatMap(person -> {

            CompletableFuture<PutItemResponse> responseCompletableFuture = dynamoDbService.savePerson(person);

            return Mono.fromFuture(responseCompletableFuture);

        }).flatMap(pdb -> ServerResponse.ok().build());
    }

    public Mono<ServerResponse> edit(ServerRequest request) {
        return ServerResponse.ok().build();
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        return ServerResponse.ok().build();
    }

}