package com.npocmaka.local.dynamo.descriptors;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;


import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalSecondaryIndexDesc implements KeySchemaHolder {

    @JsonProperty("IndexName")
    @NonNull
    private String name;

    @JsonProperty("Projection")
    @NonNull
    private ProjectionDesc projection;

    @JsonProperty("KeySchema")
    @NonNull
    private List<KeySchemaAttributeDesc> keySchema;

}
