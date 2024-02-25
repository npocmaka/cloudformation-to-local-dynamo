package com.npocmaka.local.dynamo;

import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class DynamoEnv {
    @NonNull
    @JsonIgnore
    DynamoDBProxyServer server;

    @NonNull
    @JsonIgnore
    DynamoDbClient client;

    @NonNull
    @JsonProperty("URI")
    String uri;

    @NonNull
    @JsonProperty("PORT")
    Integer port;

    @JsonProperty("PID")
    long pid;

    @JsonProperty("Tables")
    List<TableEnv> tables;

    DynamoEnv(@NonNull DynamoDBProxyServer server, @NonNull DynamoDbClient client, @NonNull String uri, int port ) {
        this.server =  server;
        this.client = client;
        this.uri = uri;
        this.port = port;
    }
}
