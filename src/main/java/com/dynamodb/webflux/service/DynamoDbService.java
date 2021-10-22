package com.dynamodb.webflux.service;

import com.dynamodb.webflux.domain.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class DynamoDbService {

    public static final String TABLE_NAME = "persons";
    public static final String ID_COLUMN = "id";
    public static final String NAME_COLUMN = "name";

    final DynamoDbAsyncClient client;

    @Autowired
    public DynamoDbService(DynamoDbAsyncClient client) {
        this.client = client;
    }

    //Creating table on startup if not exists
    // @PostConstruct
    public void createTableIfNeeded() throws ExecutionException, InterruptedException {
        ListTablesRequest request = ListTablesRequest
                .builder()
                .exclusiveStartTableName(TABLE_NAME)
                .build();
        CompletableFuture<ListTablesResponse> listTableResponse = client.listTables(request);

        CompletableFuture<CreateTableResponse> createTableRequest = listTableResponse
                .thenCompose(response -> {
                    boolean tableExist = response
                            .tableNames()
                            .contains(TABLE_NAME);

                    if (!tableExist) {
                        return createTable();
                    } else {
                        return CompletableFuture.completedFuture(null);
                    }
                });

        //Wait in synchronous manner for table creation
        createTableRequest.get();
    }

    public CompletableFuture<PutItemResponse> savePerson(Person person) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(ID_COLUMN, AttributeValue.builder().s(person.getId()).build());
        item.put(NAME_COLUMN, AttributeValue.builder().s(person.getName()).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        return client.putItem(putItemRequest);
    }

    public CompletableFuture<Person> getPerson(String id) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(ID_COLUMN, AttributeValue.builder().s(id).build());

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .attributesToGet(NAME_COLUMN)
                .build();

        return client.getItem(getRequest).thenApply(item -> {
            if (!item.hasItem()) {
                return null;
            } else {
                Map<String, AttributeValue> itemAttr = item.item();
                String body = itemAttr.get(NAME_COLUMN).s();
                return new Person(id, body);
            }
        });
    }

    private CompletableFuture<CreateTableResponse> createTable() {
        KeySchemaElement keySchemaElement = KeySchemaElement
                .builder()
                .attributeName(ID_COLUMN)
                .keyType(KeyType.HASH)
                .build();

        AttributeDefinition attributeDefinition = AttributeDefinition
                .builder()
                .attributeName(ID_COLUMN)
                .attributeType(ScalarAttributeType.S)
                .build();

        CreateTableRequest request = CreateTableRequest.builder()
                .tableName(TABLE_NAME)
                .keySchema(keySchemaElement)
                .attributeDefinitions(attributeDefinition)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        return client.createTable(request);
    }
}

