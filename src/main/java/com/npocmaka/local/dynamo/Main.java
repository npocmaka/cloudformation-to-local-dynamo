package com.npocmaka.local.dynamo;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndexDescription;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import com.npocmaka.local.dynamo.descriptors.TableDesc;
import software.amazon.awssdk.services.dynamodb.model.LocalSecondaryIndexDescription;


public class Main {
    public static void main (String[] args){
        if(args.length == 0) {
            System.err.println("not enough arguments");
            System.exit(1);
        }

        DynamoEnv env = startDynamo(args[0]);

        ObjectMapper mapper = new ObjectMapper();

        try {

            String envStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(env);
            System.out.println(envStr);
            if(args.length > 1) {
                Path path = Paths.get(args[1]);
                Files.write(path, envStr.getBytes());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static DynamoEnv startDynamo(String filePath){
        TablesDescInitializer ti = new TablesDescInitializer(filePath);
        DynamoEnv env = null;
        try {
            //---
            List<TableDesc> tables = ti.initializeTables();
            env = DynamoEnvCreator.createDynamoEnv();
            TableCreator creator = new TableCreator(tables, env.getClient());
            creator.createTables();
            //---

            env.pid = ProcessHandle.current().pid();
            env.tables = listAllTables(env.getClient());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return env;
    }


    public static List<TableEnv> listAllTables(DynamoDbClient client) {
        boolean moreTables = true;
        String lastName = null;

        List<TableEnv> tables = new LinkedList<>();
        while (moreTables) {
            try {
                ListTablesResponse response;
                ListTablesRequest request;
                if (lastName == null) {
                    request = ListTablesRequest.builder().build();
                } else {
                    request = ListTablesRequest.builder()
                            .exclusiveStartTableName(lastName).build();
                }
                response = client.listTables(request);

                List<String> tableNames = response.tableNames();
                if (tableNames.size() > 0) {
                    for (String curName : tableNames) {
                        TableEnv tableEnv = new TableEnv(curName);
                        DescribeTableRequest describeTableRequest = DescribeTableRequest.builder()
                                .tableName(curName)
                                .build();
                        DescribeTableResponse describeTableResponse = client.describeTable(describeTableRequest);

                        if(describeTableResponse.table().localSecondaryIndexes().size() > 0) {
                            for (LocalSecondaryIndexDescription lsi : describeTableResponse.table().localSecondaryIndexes()) {
                                String lsiE = tableEnv.getLocalIndexes() + ":" + lsi.indexName();
                                tableEnv.setLocalIndexes(lsiE);
                            }
                        } else {
                            tableEnv.setLocalIndexes(null);
                        }

                        if(describeTableResponse.table().globalSecondaryIndexes().size() >0) {
                            for (GlobalSecondaryIndexDescription gsi : describeTableResponse.table().globalSecondaryIndexes()) {
                                String gsiE = tableEnv.getGlobalIndexes() + ":" + gsi.indexName();
                                tableEnv.setGlobalIndexes(gsiE);
                            }
                        } else {
                            tableEnv.setGlobalIndexes(null);
                        }

                        tables.add(tableEnv);
                    }
                }

                lastName = response.lastEvaluatedTableName();
                if (lastName == null) {
                    moreTables = false;
                }
            } catch (DynamoDbException e) {
                System.exit(1);
            }
        }
        return tables;
    }
}
