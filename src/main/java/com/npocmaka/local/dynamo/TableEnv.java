package com.npocmaka.local.dynamo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableEnv {

    @JsonProperty("TableName")
    @NonNull
    String tableName;

    @JsonProperty("LocalIndexes")
    String localIndexes;

    @JsonProperty("GlobalIndexes")
    String globalIndexes;

    public TableEnv(@NonNull String name) {
        this.tableName = name;
        this.localIndexes = "";
        this.globalIndexes = "";
    }
}
