/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.internal;

import com.arangodb.entity.VertexEntity;
import com.arangodb.entity.VertexUpdateEntity;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.util.DocumentUtil;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.model.*;
import com.arangodb.Request;
import com.arangodb.RequestType;

/**
 * @author Mark Vollmary
 */
public abstract class InternalArangoVertexCollection<A extends InternalArangoDB<E>,
        D extends InternalArangoDatabase<A, E>, G extends InternalArangoGraph<A, D, E>, E extends ArangoExecutor>
        extends ArangoExecuteable<E> {

    private static final String PATH_API_GHARIAL = "/_api/gharial";
    private static final String VERTEX_PATH = "vertex";
    private static final String VERTEX_JSON_POINTER = "/vertex";
    private static final String TRANSACTION_ID = "x-arango-trx-id";

    private final G graph;
    private final String name;

    protected InternalArangoVertexCollection(final G graph, final String name) {
        super(graph.executor, graph.serde);
        this.graph = graph;
        this.name = name;
    }

    public G graph() {
        return graph;
    }

    public String name() {
        return name;
    }

    protected Request dropRequest() {
        return request(graph.db().dbName(), RequestType.DELETE, PATH_API_GHARIAL, graph.name(), VERTEX_PATH, name);
    }

    protected <T> Request insertVertexRequest(final T value, final VertexCreateOptions options) {
        final Request request = request(graph.db().dbName(), RequestType.POST, PATH_API_GHARIAL, graph.name(), VERTEX_PATH,
                name);
        final VertexCreateOptions params = (options != null ? options : new VertexCreateOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.setBody(getSerde().serializeUserData(value));
        return request;
    }

    protected ResponseDeserializer<VertexEntity> insertVertexResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), VERTEX_JSON_POINTER, VertexEntity.class);
    }

    protected Request getVertexRequest(final String key, final GraphDocumentReadOptions options) {
        final Request request = request(graph.db().dbName(), RequestType.GET, PATH_API_GHARIAL, graph.name(), VERTEX_PATH,
                DocumentUtil.createDocumentHandle(name, key));
        final GraphDocumentReadOptions params = (options != null ? options : new GraphDocumentReadOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putHeaderParam(ArangoRequestParam.IF_NONE_MATCH, params.getIfNoneMatch());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        if (Boolean.TRUE.equals(params.getAllowDirtyRead())) {
            RequestUtils.allowDirtyRead(request);
        }
        return request;
    }

    protected <T> ResponseDeserializer<T> getVertexResponseDeserializer(final Class<T> type) {
        return response -> getSerde().deserializeUserData(getSerde().extract(response.getBody(), VERTEX_JSON_POINTER), type);
    }

    protected <T> Request replaceVertexRequest(final String key, final T value, final VertexReplaceOptions options) {
        final Request request = request(graph.db().dbName(), RequestType.PUT, PATH_API_GHARIAL, graph.name(), VERTEX_PATH,
                DocumentUtil.createDocumentHandle(name, key));
        final VertexReplaceOptions params = (options != null ? options : new VertexReplaceOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.setBody(getSerde().serializeUserData(value));
        return request;
    }

    protected ResponseDeserializer<VertexUpdateEntity> replaceVertexResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), VERTEX_JSON_POINTER, VertexUpdateEntity.class);
    }

    protected <T> Request updateVertexRequest(final String key, final T value, final VertexUpdateOptions options) {
        final Request request;
        request = request(graph.db().dbName(), RequestType.PATCH, PATH_API_GHARIAL, graph.name(), VERTEX_PATH,
                DocumentUtil.createDocumentHandle(name, key));
        final VertexUpdateOptions params = (options != null ? options : new VertexUpdateOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.KEEP_NULL, params.getKeepNull());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.setBody(getSerde().serializeUserData(value));
        return request;
    }

    protected ResponseDeserializer<VertexUpdateEntity> updateVertexResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), VERTEX_JSON_POINTER, VertexUpdateEntity.class);
    }

    protected Request deleteVertexRequest(final String key, final VertexDeleteOptions options) {
        final Request request = request(graph.db().dbName(), RequestType.DELETE, PATH_API_GHARIAL, graph.name(),
                VERTEX_PATH,
                DocumentUtil.createDocumentHandle(name, key));
        final VertexDeleteOptions params = (options != null ? options : new VertexDeleteOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        return request;
    }

}