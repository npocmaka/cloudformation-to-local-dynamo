package com.npocmaka.local.dynamo.descriptors;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class AttributeDesc {

    @JsonProperty("AttributeName")
    @NonNull
    String name;

    @JsonProperty("AttributeType")
    @NonNull
    AttributeType type;
}
