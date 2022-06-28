package com.arangodb.serde;

import com.arangodb.ArangoDBException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

class InternalSerdeImpl extends JacksonSerdeImpl implements InternalSerde {
    private final ObjectMapper jsonMapper = new ObjectMapper();

    InternalSerdeImpl(DataType dataType, ObjectMapper mapper) {
        super(dataType, mapper);
    }

    @Override
    public String toJsonString(final byte[] content) {
        switch (dataType) {
            case JSON:
                return new String(content, StandardCharsets.UTF_8);
            case VPACK:
                try {
                    JsonNode tree = mapper.readTree(content);
                    return jsonMapper.writeValueAsString(tree);
                } catch (IOException e) {
                    throw new ArangoDBException(e);
                }
            default:
                throw new IllegalStateException("Unexpected value: " + dataType);
        }
    }

    @Override
    public byte[] extract(final byte[] content, final String jsonPointer) {
        try {
            JsonNode target = mapper.readTree(content).at(jsonPointer);
            return mapper.writeValueAsBytes(target);
        } catch (IOException e) {
            throw new ArangoDBException(e);
        }
    }

}
