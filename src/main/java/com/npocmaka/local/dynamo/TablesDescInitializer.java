package com.npocmaka.local.dynamo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import net.minidev.json.JSONObject;
import org.yaml.snakeyaml.Yaml;
import com.npocmaka.local.dynamo.descriptors.TableDesc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@AllArgsConstructor
public class TablesDescInitializer {
    String pathToFile;


    public List<TableDesc> initializeTables() throws Exception {
        final String yaml = ".yaml";
        final String json = ".json";

        String content;
        if(pathToFile.toLowerCase().endsWith(yaml)) {
            // Yaml does not allow getting nested objects ,so we need a json
            content = yamlToJsonString(pathToFile);
        } else if(pathToFile.toLowerCase().endsWith(json)) {
            content = Files.readString(Path.of(pathToFile), StandardCharsets.UTF_8);
        } else {
            throw new IllegalArgumentException("Unknown file extension for " + pathToFile);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> tables = tablesAsJsons(content, objectMapper);
        return toTableObjects(tables, objectMapper);
    }

    private String yamlToJsonString(String filePath) throws Exception {
        String OUTPUTS = "Outputs:";

        StringBuilder res = new StringBuilder();
        BufferedReader reader;
        try {
            reader = new BufferedReader(
                    new FileReader(filePath)
            );

            String line = reader.readLine();
            while (line != null) {
                if (line.contains(OUTPUTS)) {
                    break;
                }
                res.append("\n").append(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        InputStream inputStream = new ByteArrayInputStream(res.toString().getBytes(StandardCharsets.UTF_8));

        Yaml yaml = new Yaml();
        Map<String, Object> obj = yaml.load(inputStream);

        JSONObject jsonObject = new JSONObject(obj);
        return jsonObject.toJSONString();
    }

    List<String> tablesAsJsons(String json, ObjectMapper objectMapper) throws JsonProcessingException {
        String tableType = "AWS::DynamoDB::Table";
        String globalTableType = "AWS::DynamoDB::GlobalTable";
        String resources = "Resources";
        String properties = "Properties";
        String tableName = "TableName";

        JsonNode jsonNode = objectMapper.readTree(json);
        List<String> tables = new LinkedList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.get(resources).fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();

            String entryType = entry.getValue().get("Type").asText();
            if (entryType.equals(tableType) || entryType.equals(globalTableType)) {
                JsonNode props = jsonNode.get(resources).get(entry.getKey()).get(properties);

                boolean hasName = props.has(tableName);
                if (!hasName) {
                    ((ObjectNode) props).put(tableName, entry.getKey());
                    jsonNode.get(resources).get(entry.getKey()).get(properties);
                }
                tables.add(props.toString());
            }
        }
        return tables;
    }

    List<TableDesc> toTableObjects(List<String> jsonStrings, ObjectMapper mapper) throws JsonProcessingException {
        List<TableDesc> tables = new ArrayList<>(jsonStrings.size());
        for (String s : jsonStrings) {
            try {
                TableDesc table = mapper.readValue(s, TableDesc.class);
                tables.add(table);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        return tables;
    }
}
