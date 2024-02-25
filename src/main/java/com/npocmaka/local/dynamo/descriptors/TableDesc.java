package com.npocmaka.local.dynamo.descriptors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;


@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class TableDesc implements KeySchemaHolder {

    @JsonProperty("TableName")
    @NonNull
    String name;

    @JsonProperty("AttributeDefinitions")
    @NonNull
    List<AttributeDesc> attributeDefinitions;

    @JsonProperty("KeySchema")
    @NonNull
    private List<KeySchemaAttributeDesc> keySchema;

    @JsonProperty("GlobalSecondaryIndexes")
    List<GlobalSecondaryIndexDesc> globalIndexes;

    @JsonProperty("LocalSecondaryIndexes")
    List<LocalSecondaryIndexDesc> localIndexes;

    @JsonProperty("TimeToLiveSpecification")
    TimeToLiveSpecificationDesc ttl;

    @JsonProperty("ProvisionedThroughput")
    ProvisionedThroughputDesc throughput =
            new ProvisionedThroughputDesc(1,1);//default values

    public void validate(){
        KeySchemaAttributeDesc hash = getHashKey();

        List<String> attributes = getAttributeDefinitions().stream()
                .map(AttributeDesc::getName)
                .collect(Collectors.toList());

        if(!attributes.contains(hash.getName())) {
            throw new IllegalStateException("Hash key not listed in attributes definitions");
        }

        if(getLocalIndexes() != null && getLocalIndexes().size() > 0) {
            for(LocalSecondaryIndexDesc lsi : getLocalIndexes()) {
                if(!attributes.contains(lsi.getHashKey().getName())) {
                    throw new
                            IllegalStateException("Hash key not listed in attributes definitions for global index " + lsi.getName());
                }
            }
        }

        if(getGlobalIndexes() != null && getGlobalIndexes().size() > 0) {
            for(GlobalSecondaryIndexDesc gsi : getGlobalIndexes()) {
                if(!attributes.contains(gsi.getHashKey().getName())) {
                    throw new
                            IllegalStateException("Hash key not listed in attributes definitions for local index " + gsi.getName());
                }
            }
        }

        List<String> ksAttributes = keySchema.stream()
                .map(KeySchemaAttributeDesc::getName)
                .collect(Collectors.toList());

        if(localIndexes != null) {
            for (LocalSecondaryIndexDesc lsi : getLocalIndexes()) {
                List<String> lsiKsAttributes = lsi.getKeySchema()
                        .stream()
                        .map(KeySchemaAttributeDesc::getName)
                        .collect(Collectors.toList());
                ksAttributes.addAll(lsiKsAttributes);
            }
        }
        if (globalIndexes != null) {
            for (GlobalSecondaryIndexDesc gsi : getGlobalIndexes()) {
                List<String> gsiKsAttributes = gsi.getKeySchema()
                        .stream()
                        .map(KeySchemaAttributeDesc::getName)
                        .collect(Collectors.toList());
                ksAttributes.addAll(gsiKsAttributes);
            }
        }

        for(String attr : attributes) {
            if(!ksAttributes.contains(attr)) {
                throw new IllegalStateException("redundant attribute definition: " + attr);
            }
        }
    }
}
