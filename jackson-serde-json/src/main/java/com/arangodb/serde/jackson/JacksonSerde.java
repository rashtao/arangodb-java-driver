package com.arangodb.serde.jackson;

import com.arangodb.ContentType;
import com.arangodb.internal.serde.SerdeUtils;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.RequestContext;
import com.arangodb.serde.jackson.internal.JacksonSerdeImpl;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

/**
 * User data serde based on Jackson Databind. Not shaded in arangodb-java-driver-shaded.
 */
public interface JacksonSerde extends ArangoSerde {

    /**
     * Creates a new JacksonSerde with default settings for the specified data type.
     *
     * @param contentType serialization target data type
     * @return the created JacksonSerde
     */
    static JacksonSerde of(final ContentType contentType) {
        return create(JacksonMapperProvider.of(contentType));
    }

    /**
     * Creates a new JacksonSerde using the provided ObjectMapper.
     *
     * @param mapper Jackson ObjectMapper to use
     * @return the created JacksonSerde
     */
    static JacksonSerde create(final ObjectMapper mapper) {
        return new JacksonSerdeImpl(mapper);
    }

    /**
     * Extracts the {@link RequestContext} from the current {@link DeserializationContext}.
     *
     * @param ctx current Jackson {@link DeserializationContext}
     * @return current {@link RequestContext}
     */
    static RequestContext getRequestContext(DeserializationContext ctx) {
        return (RequestContext) ctx.getAttribute(SerdeUtils.SERDE_CONTEXT_ATTRIBUTE_NAME);
    }

    /**
     * Allows configuring the underlying Jackson ObjectMapper
     *
     * @param configureFunction function to configure the Jackson ObjectMapper
     */
    JacksonSerde configure(final Consumer<ObjectMapper> configureFunction);

}
