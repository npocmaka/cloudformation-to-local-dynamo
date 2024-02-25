package com.npocmaka.local.dynamo.descriptors;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class KeySchemaAttributeDesc {

    @JsonProperty("AttributeName")
    @NonNull
    String name;

    @JsonProperty("KeyType")
    @NonNull
    KeySchemaAttributeType attributeType;

}
