package com.npocmaka.local.dynamo;

import com.npocmaka.local.dynamo.descriptors.AttributeDesc;
import com.npocmaka.local.dynamo.descriptors.GlobalSecondaryIndexDesc;
import com.npocmaka.local.dynamo.descriptors.KeySchemaAttributeDesc;
import com.npocmaka.local.dynamo.descriptors.LocalSecondaryIndexDesc;
import com.npocmaka.local.dynamo.descriptors.ProjectionDesc;
import com.npocmaka.local.dynamo.descriptors.ProvisionedThroughputDesc;
import com.npocmaka.local.dynamo.descriptors.TableDesc;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.LocalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.TimeToLiveSpecification;
import software.amazon.awssdk.services.dynamodb.model.UpdateTimeToLiveRequest;

import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
@Data
public class TableCreator {
    @NonNull
    List<TableDesc> tables;

    @NonNull
    DynamoDbClient ddbClient;


    void createTables(){

        for(TableDesc table: tables) {
            table.validate();

            CreateTableRequest.Builder builder = CreateTableRequest
                    .builder().tableName(table.getName());

            builder = builder.attributeDefinitions(toAttributeDefinitions(table.getAttributeDefinitions()));
            builder = builder.keySchema(toKeySchema(table.getKeySchema()));


            /// --- GSI ---
            if( table.getGlobalIndexes() != null && table.getGlobalIndexes().size() > 0) {
                List<GlobalSecondaryIndex> gsiIndexes = new ArrayList<>(table.getGlobalIndexes().size());
                for(GlobalSecondaryIndexDesc gsi : table.getGlobalIndexes()){

                    GlobalSecondaryIndex.Builder gsiBuilder = GlobalSecondaryIndex
                            .builder().indexName(gsi.getName());

                    gsiBuilder = gsiBuilder.keySchema(toKeySchema(gsi.getKeySchema()));

                    gsiBuilder = gsiBuilder.projection(toProjection(gsi.getProjection()));
                    gsiBuilder = gsiBuilder.provisionedThroughput(toThroughput(gsi.getThroughput()));
                    gsiIndexes.add(gsiBuilder.build());
                }
                builder = builder.globalSecondaryIndexes(gsiIndexes);
            }

            // --- LSI ---
            if(table.getLocalIndexes() != null && table.getLocalIndexes().size() > 0) {
                List<LocalSecondaryIndex> lsiIndexes = new ArrayList<>(table.getLocalIndexes().size());
                for(LocalSecondaryIndexDesc lsi : table.getLocalIndexes()){
                    LocalSecondaryIndex.Builder lsiBuilder = LocalSecondaryIndex
                            .builder().indexName(lsi.getName());

                    lsiBuilder = lsiBuilder.keySchema(toKeySchema(lsi.getKeySchema()));

                    lsiBuilder = lsiBuilder.projection(toProjection(lsi.getProjection()));
                    lsiIndexes.add(lsiBuilder.build());
                }
                builder = builder.localSecondaryIndexes(lsiIndexes);
            }

            builder = builder.provisionedThroughput(toThroughput(table.getThroughput()));

            ddbClient.createTable(builder.build());

            if(table.getTtl() != null) {
                TimeToLiveSpecification.Builder ttlBuilder = TimeToLiveSpecification.builder();
                ttlBuilder = ttlBuilder.attributeName(table.getTtl().getAttributeName())
                        .enabled(table.getTtl().getEnabled());

                ddbClient.updateTimeToLive(
                        UpdateTimeToLiveRequest.builder()
                                .tableName(table.getName())
                                .timeToLiveSpecification(ttlBuilder.build())
                                .build()
                );
            }
        }
    }

    List<AttributeDefinition> toAttributeDefinitions(List<AttributeDesc> descriptions){
        List<AttributeDefinition> attributes = new ArrayList<>(descriptions.size());
        for(AttributeDesc attr : descriptions) {
            AttributeDefinition.Builder attrBuilder = AttributeDefinition.builder();
            attrBuilder = attrBuilder
                    .attributeName(attr.getName())
                    .attributeType(attr.getType().name());
            attributes.add(attrBuilder.build());
        }
        return attributes;
    }

    List<KeySchemaElement> toKeySchema(List<KeySchemaAttributeDesc> ksElements){
        List<KeySchemaElement> elements = new ArrayList<>(ksElements.size());
        for(KeySchemaAttributeDesc ks : ksElements) {
            KeySchemaElement.Builder ksBuilder = KeySchemaElement.builder();
            ksBuilder = ksBuilder
                    .attributeName(ks.getName())
                    .keyType(ks.getAttributeType().name());
            elements.add(ksBuilder.build());
        }
        return elements;
    }

    Projection toProjection(ProjectionDesc description) {
        return Projection.builder().projectionType(
                description.getProjectionType().name()
        ).build();
    }

    ProvisionedThroughput toThroughput(ProvisionedThroughputDesc description){
        return ProvisionedThroughput.builder()
                        .readCapacityUnits(description.getReadCapacityUnits())
                        .writeCapacityUnits(description.getWriteCapacityUnits()).build();
    }
}
