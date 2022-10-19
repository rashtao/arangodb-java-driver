package com.arangodb.internal.serde;

import com.arangodb.ContentType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Supplier;

public interface MapperProvider extends Supplier<ObjectMapper> {
    static ObjectMapper of(final ContentType contentType) {
        if (contentType == ContentType.JSON) {
            return JsonMapperProvider.INSTANCE.get();
        } else if (contentType == ContentType.VPACK) {
            return VPackMapperProvider.INSTANCE.get();
        } else {
            throw new IllegalArgumentException("Unexpected value: " + contentType);
        }
    }
}