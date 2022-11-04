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

import com.arangodb.DbName;
import com.arangodb.entity.*;
import com.arangodb.entity.arangosearch.analyzer.SearchAnalyzer;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.model.*;
import com.arangodb.model.arangosearch.*;
import com.arangodb.Request;
import com.arangodb.RequestType;

import java.util.Collection;
import java.util.Map;

import static com.arangodb.internal.serde.SerdeUtils.constructListType;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public abstract class InternalArangoDatabase<A extends InternalArangoDB<EXECUTOR>, EXECUTOR extends ArangoExecutor> extends ArangoExecuteable<EXECUTOR> {

    protected static final String PATH_API_DATABASE = "/_api/database";
    private static final String PATH_API_VERSION = "/_api/version";
    private static final String PATH_API_ENGINE = "/_api/engine";
    private static final String PATH_API_CURSOR = "/_api/cursor";
    private static final String PATH_API_TRANSACTION = "/_api/transaction";
    private static final String PATH_API_BEGIN_STREAM_TRANSACTION = "/_api/transaction/begin";
    private static final String PATH_API_AQLFUNCTION = "/_api/aqlfunction";
    private static final String PATH_API_EXPLAIN = "/_api/explain";
    private static final String PATH_API_QUERY = "/_api/query";
    private static final String PATH_API_QUERY_CACHE = "/_api/query-cache";
    private static final String PATH_API_QUERY_CACHE_PROPERTIES = "/_api/query-cache/properties";
    private static final String PATH_API_QUERY_PROPERTIES = "/_api/query/properties";
    private static final String PATH_API_QUERY_CURRENT = "/_api/query/current";
    private static final String PATH_API_QUERY_SLOW = "/_api/query/slow";
    private static final String PATH_API_ADMIN_ROUTING_RELOAD = "/_admin/routing/reload";
    private static final String PATH_API_USER = "/_api/user";

    private static final String TRANSACTION_ID = "x-arango-trx-id";

    private final DbName dbName;
    private final A arango;

    protected InternalArangoDatabase(final A arango, final DbName dbName) {
        super(arango.executor, arango.serde);
        this.arango = arango;
        this.dbName = dbName;
    }

    public A arango() {
        return arango;
    }

    public DbName dbName() {
        return dbName;
    }

    protected ResponseDeserializer<Collection<String>> getDatabaseResponseDeserializer() {
        return arango.getDatabaseResponseDeserializer();
    }

    protected Request getAccessibleDatabasesRequest() {
        return request(dbName, RequestType.GET, PATH_API_DATABASE, "user");
    }

    protected Request getVersionRequest() {
        return request(dbName, RequestType.GET, PATH_API_VERSION);
    }

    protected Request getEngineRequest() {
        return request(dbName, RequestType.GET, PATH_API_ENGINE);
    }

    protected Request createCollectionRequest(final String name, final CollectionCreateOptions options) {

        byte[] body = getSerde().serialize(OptionsBuilder.build(options != null ? options :
                new CollectionCreateOptions(), name));

        return request(dbName, RequestType.POST, InternalArangoCollection.PATH_API_COLLECTION).setBody(body);
    }

    protected Request getCollectionsRequest(final CollectionsReadOptions options) {
        final Request request;
        request = request(dbName, RequestType.GET, InternalArangoCollection.PATH_API_COLLECTION);
        final CollectionsReadOptions params = (options != null ? options : new CollectionsReadOptions());
        request.putQueryParam("excludeSystem", params.getExcludeSystem());
        return request;
    }

    protected ResponseDeserializer<Collection<CollectionEntity>> getCollectionsResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                constructListType(CollectionEntity.class));
    }

    protected Request dropRequest() {
        return request(DbName.SYSTEM, RequestType.DELETE, PATH_API_DATABASE, dbName.get());
    }

    protected ResponseDeserializer<Boolean> createDropResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                Boolean.class);
    }

    protected Request grantAccessRequest(final String user, final Permissions permissions) {
        return request(DbName.SYSTEM, RequestType.PUT, PATH_API_USER, user, ArangoRequestParam.DATABASE,
                dbName.get()).setBody(getSerde().serialize(OptionsBuilder.build(new UserAccessOptions(), permissions)));
    }

    protected Request resetAccessRequest(final String user) {
        return request(DbName.SYSTEM, RequestType.DELETE, PATH_API_USER, user, ArangoRequestParam.DATABASE,
                dbName.get());
    }

    protected Request updateUserDefaultCollectionAccessRequest(final String user, final Permissions permissions) {
        return request(DbName.SYSTEM, RequestType.PUT, PATH_API_USER, user, ArangoRequestParam.DATABASE, dbName.get()
                , "*").setBody(getSerde().serialize(OptionsBuilder.build(new UserAccessOptions(), permissions)));
    }

    protected Request getPermissionsRequest(final String user) {
        return request(DbName.SYSTEM, RequestType.GET, PATH_API_USER, user, ArangoRequestParam.DATABASE, dbName.get());
    }

    protected ResponseDeserializer<Permissions> getPermissionsResponseDeserialzer() {
        return response -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                Permissions.class);
    }

    protected Request queryRequest(final String query, final Map<String, Object> bindVars,
                                   final AqlQueryOptions options) {
        final AqlQueryOptions opt = options != null ? options : new AqlQueryOptions();
        final Request request = request(dbName, RequestType.POST, PATH_API_CURSOR)
                .setBody(getSerde().serialize(OptionsBuilder.build(opt, query, bindVars)));
        if (Boolean.TRUE.equals(opt.getAllowDirtyRead())) {
            RequestUtils.allowDirtyRead(request);
        }
        request.putHeaderParam(TRANSACTION_ID, opt.getStreamTransactionId());
        return request;
    }

    protected Request queryNextRequest(final String id, final AqlQueryOptions options, Map<String, String> meta) {

        final Request request = request(dbName, RequestType.POST, PATH_API_CURSOR, id);
        request.putHeaderParams(meta);

        final AqlQueryOptions opt = options != null ? options : new AqlQueryOptions();

        if (Boolean.TRUE.equals(opt.getAllowDirtyRead())) {
            RequestUtils.allowDirtyRead(request);
        }
        request.putHeaderParam(TRANSACTION_ID, opt.getStreamTransactionId());
        return request;
    }

    protected Request queryCloseRequest(final String id, final AqlQueryOptions options, Map<String, String> meta) {

        final Request request = request(dbName, RequestType.DELETE, PATH_API_CURSOR, id);
        request.putHeaderParams(meta);

        final AqlQueryOptions opt = options != null ? options : new AqlQueryOptions();

        if (Boolean.TRUE.equals(opt.getAllowDirtyRead())) {
            RequestUtils.allowDirtyRead(request);
        }
        request.putHeaderParam(TRANSACTION_ID, opt.getStreamTransactionId());
        return request;
    }

    protected Request explainQueryRequest(final String query, final Map<String, Object> bindVars,
                                          final AqlQueryExplainOptions options) {
        final AqlQueryExplainOptions opt = options != null ? options : new AqlQueryExplainOptions();
        return request(dbName, RequestType.POST, PATH_API_EXPLAIN)
                .setBody(getSerde().serialize(OptionsBuilder.build(opt, query, bindVars)));
    }

    protected Request parseQueryRequest(final String query) {
        return request(dbName, RequestType.POST, PATH_API_QUERY).setBody(getSerde().serialize(OptionsBuilder.build(new AqlQueryParseOptions(), query)));
    }

    protected Request clearQueryCacheRequest() {
        return request(dbName, RequestType.DELETE, PATH_API_QUERY_CACHE);
    }

    protected Request getQueryCachePropertiesRequest() {
        return request(dbName, RequestType.GET, PATH_API_QUERY_CACHE_PROPERTIES);
    }

    protected Request setQueryCachePropertiesRequest(final QueryCachePropertiesEntity properties) {
        return request(dbName, RequestType.PUT, PATH_API_QUERY_CACHE_PROPERTIES).setBody(getSerde().serialize(properties));
    }

    protected Request getQueryTrackingPropertiesRequest() {
        return request(dbName, RequestType.GET, PATH_API_QUERY_PROPERTIES);
    }

    protected Request setQueryTrackingPropertiesRequest(final QueryTrackingPropertiesEntity properties) {
        return request(dbName, RequestType.PUT, PATH_API_QUERY_PROPERTIES).setBody(getSerde().serialize(properties));
    }

    protected Request getCurrentlyRunningQueriesRequest() {
        return request(dbName, RequestType.GET, PATH_API_QUERY_CURRENT);
    }

    protected Request getSlowQueriesRequest() {
        return request(dbName, RequestType.GET, PATH_API_QUERY_SLOW);
    }

    protected Request clearSlowQueriesRequest() {
        return request(dbName, RequestType.DELETE, PATH_API_QUERY_SLOW);
    }

    protected Request killQueryRequest(final String id) {
        return request(dbName, RequestType.DELETE, PATH_API_QUERY, id);
    }

    protected Request createAqlFunctionRequest(final String name, final String code,
                                               final AqlFunctionCreateOptions options) {
        return request(dbName, RequestType.POST, PATH_API_AQLFUNCTION).setBody(getSerde().serialize(OptionsBuilder.build(options != null ? options : new AqlFunctionCreateOptions(), name, code)));
    }

    protected Request deleteAqlFunctionRequest(final String name, final AqlFunctionDeleteOptions options) {
        final Request request = request(dbName, RequestType.DELETE, PATH_API_AQLFUNCTION, name);
        final AqlFunctionDeleteOptions params = options != null ? options : new AqlFunctionDeleteOptions();
        request.putQueryParam("group", params.getGroup());
        return request;
    }

    protected ResponseDeserializer<Integer> deleteAqlFunctionResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), "/deletedCount", Integer.class);
    }

    protected Request getAqlFunctionsRequest(final AqlFunctionGetOptions options) {
        final Request request = request(dbName, RequestType.GET, PATH_API_AQLFUNCTION);
        final AqlFunctionGetOptions params = options != null ? options : new AqlFunctionGetOptions();
        request.putQueryParam("namespace", params.getNamespace());
        return request;
    }

    protected ResponseDeserializer<Collection<AqlFunctionEntity>> getAqlFunctionsResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                constructListType(AqlFunctionEntity.class));
    }

    protected Request createGraphRequest(final String name, final Collection<EdgeDefinition> edgeDefinitions,
                                         final GraphCreateOptions options) {
        return request(dbName, RequestType.POST, InternalArangoGraph.PATH_API_GHARIAL).setBody(getSerde().serialize(OptionsBuilder.build(options != null ? options : new GraphCreateOptions(), name, edgeDefinitions)));
    }

    protected ResponseDeserializer<GraphEntity> createGraphResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), "/graph", GraphEntity.class);
    }

    protected Request getGraphsRequest() {
        return request(dbName, RequestType.GET, InternalArangoGraph.PATH_API_GHARIAL);
    }

    protected ResponseDeserializer<Collection<GraphEntity>> getGraphsResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), "/graphs",
                constructListType(GraphEntity.class));
    }

    protected Request transactionRequest(final String action, final TransactionOptions options) {
        return request(dbName, RequestType.POST, PATH_API_TRANSACTION).setBody(getSerde().serialize(OptionsBuilder.build(options != null ? options : new TransactionOptions(), action)));
    }

    protected <T> ResponseDeserializer<T> transactionResponseDeserializer(final Class<T> type) {
        return response -> {
            byte[] userContent = getSerde().extract(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER);
            return getSerde().deserializeUserData(userContent, type);
        };
    }

    protected Request beginStreamTransactionRequest(final StreamTransactionOptions options) {
        StreamTransactionOptions opts = options != null ? options : new StreamTransactionOptions();
        Request r = request(dbName, RequestType.POST, PATH_API_BEGIN_STREAM_TRANSACTION).setBody(getSerde().serialize(opts));
        if(Boolean.TRUE.equals(opts.getAllowDirtyRead())) {
            RequestUtils.allowDirtyRead(r);
        }
        return r;
    }

    protected Request abortStreamTransactionRequest(String id) {
        return request(dbName, RequestType.DELETE, PATH_API_TRANSACTION, id);
    }

    protected Request getStreamTransactionsRequest() {
        return request(dbName, RequestType.GET, PATH_API_TRANSACTION);
    }

    protected Request getStreamTransactionRequest(String id) {
        return request(dbName, RequestType.GET, PATH_API_TRANSACTION, id);
    }

    protected ResponseDeserializer<Collection<TransactionEntity>> transactionsResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), "/transactions",
                constructListType(TransactionEntity.class));
    }

    protected Request commitStreamTransactionRequest(String id) {
        return request(dbName, RequestType.PUT, PATH_API_TRANSACTION, id);
    }

    protected ResponseDeserializer<StreamTransactionEntity> streamTransactionResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                StreamTransactionEntity.class);
    }

    protected Request getInfoRequest() {
        return request(dbName, RequestType.GET, PATH_API_DATABASE, "current");
    }

    protected ResponseDeserializer<DatabaseEntity> getInfoResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                DatabaseEntity.class);
    }

    protected Request reloadRoutingRequest() {
        return request(dbName, RequestType.POST, PATH_API_ADMIN_ROUTING_RELOAD);
    }

    protected Request getViewsRequest() {
        return request(dbName, RequestType.GET, InternalArangoView.PATH_API_VIEW);
    }

    protected ResponseDeserializer<Collection<ViewEntity>> getViewsResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                constructListType(ViewEntity.class));
    }

    protected Request createViewRequest(final String name, final ViewType type) {
        return request(dbName, RequestType.POST, InternalArangoView.PATH_API_VIEW).setBody(getSerde().serialize(OptionsBuilder.build(new ViewCreateOptions(), name, type)));
    }

    protected Request createArangoSearchRequest(final String name, final ArangoSearchCreateOptions options) {
        return request(dbName, RequestType.POST, InternalArangoView.PATH_API_VIEW).setBody(getSerde().serialize(ArangoSearchOptionsBuilder.build(options != null ? options : new ArangoSearchCreateOptions(), name)));
    }

    protected Request createSearchAliasRequest(final String name, final SearchAliasCreateOptions options) {
        return request(dbName, RequestType.POST, InternalArangoView.PATH_API_VIEW).setBody(getSerde().serialize(
                SearchAliasOptionsBuilder.build(options != null ? options : new SearchAliasCreateOptions(), name)));
    }

    protected Request getAnalyzerRequest(final String name) {
        return request(dbName, RequestType.GET, InternalArangoView.PATH_API_ANALYZER, name);
    }

    protected Request getAnalyzersRequest() {
        return request(dbName, RequestType.GET, InternalArangoView.PATH_API_ANALYZER);
    }

    protected ResponseDeserializer<Collection<SearchAnalyzer>> getSearchAnalyzersResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                constructListType(SearchAnalyzer.class));
    }

    protected Request createAnalyzerRequest(final SearchAnalyzer options) {
        return request(dbName, RequestType.POST, InternalArangoView.PATH_API_ANALYZER).setBody(getSerde().serialize(options));
    }

    protected Request deleteAnalyzerRequest(final String name, final AnalyzerDeleteOptions options) {
        Request request = request(dbName, RequestType.DELETE, InternalArangoView.PATH_API_ANALYZER, name);
        request.putQueryParam("force", options != null ? options.getForce() : null);
        return request;
    }

}