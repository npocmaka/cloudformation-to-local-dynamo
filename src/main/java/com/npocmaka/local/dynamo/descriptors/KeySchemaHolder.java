package com.npocmaka.local.dynamo.descriptors;

import java.util.List;

public interface KeySchemaHolder {
    List<KeySchemaAttributeDesc> getKeySchema();

    String getName();

    default KeySchemaAttributeDesc getHashKey() {
        List<KeySchemaAttributeDesc> keySchema = getKeySchema();
        for(KeySchemaAttributeDesc attribute : keySchema) {
            if (attribute.attributeType.equals(KeySchemaAttributeType.HASH)) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("No hash key presented for" + getName());
    }
}
