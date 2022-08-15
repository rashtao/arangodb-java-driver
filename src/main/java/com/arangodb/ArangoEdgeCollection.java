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

package com.arangodb;

import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.EdgeUpdateEntity;
import com.arangodb.model.*;

/**
 * Interface for operations on ArangoDB edge collection level.
 *
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-edges.html">API Documentation</a>
 */
@SuppressWarnings("UnusedReturnValue")
public interface ArangoEdgeCollection extends ArangoSerdeAccessor {

    /**
     * The the handler of the named graph the edge collection is within
     *
     * @return graph handler
     */
    ArangoGraph graph();

    /**
     * The name of the edge collection
     *
     * @return collection name
     */
    String name();

    /**
     * Creates a new edge in the collection
     *
     * @param value A representation of a single edge (POJO, {@link com.arangodb.util.RawJson} or {@link com.arangodb.util.RawBytes})
     * @return information about the edge
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-edges.html#create-an-edge">API Documentation</a>
     */
    <T> EdgeEntity insertEdge(T value) ;

    /**
     * Creates a new edge in the collection
     *
     * @param value   A representation of a single edge (POJO, {@link com.arangodb.util.RawJson} or {@link com.arangodb.util.RawBytes})
     * @param options Additional options, can be null
     * @return information about the edge
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-edges.html#create-an-edge">API Documentation</a>
     */
    <T> EdgeEntity insertEdge(T value, EdgeCreateOptions options) ;

    /**
     * Fetches an existing edge
     *
     * @param key  The key of the edge
     * @param type The type of the edge-document (POJO, {@link com.arangodb.util.RawJson} or {@link com.arangodb.util.RawBytes})
     * @return the edge identified by the key
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-edges.html#get-an-edge">API Documentation</a>
     */
    <T> T getEdge(String key, Class<T> type) ;

    /**
     * Fetches an existing edge
     *
     * @param key     The key of the edge
     * @param type    The type of the edge-document (POJO, {@link com.arangodb.util.RawJson} or {@link com.arangodb.util.RawBytes})
     * @param options Additional options, can be null
     * @return the edge identified by the key
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-edges.html#get-an-edge">API Documentation</a>
     */
    <T> T getEdge(String key, Class<T> type, GraphDocumentReadOptions options) ;

    /**
     * Replaces the edge with key with the one in the body, provided there is such a edge and no precondition is
     * violated
     *
     * @param key The key of the edge
     * @param <T> The type of the edge-document (POJO, {@link com.arangodb.util.RawJson} or {@link com.arangodb.util.RawBytes})
     * @return information about the edge
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-edges.html#replace-an-edge">API Documentation</a>
     */
    <T> EdgeUpdateEntity replaceEdge(String key, T value) ;

    /**
     * Replaces the edge with key with the one in the body, provided there is such a edge and no precondition is
     * violated
     *
     * @param key     The key of the edge
     * @param <T>     The type of the edge-document (POJO, {@link com.arangodb.util.RawJson} or {@link com.arangodb.util.RawBytes})
     * @param options Additional options, can be null
     * @return information about the edge
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-edges.html#replace-an-edge">API Documentation</a>
     */
    <T> EdgeUpdateEntity replaceEdge(String key, T value, EdgeReplaceOptions options) ;

    /**
     * Partially updates the edge identified by document-key. The value must contain a document with the attributes to
     * patch (the patch document). All attributes from the patch document will be added to the existing document if they
     * do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key The key of the edge
     * @param <T> The type of the edge-document (POJO, {@link com.arangodb.util.RawJson} or {@link com.arangodb.util.RawBytes})
     * @return information about the edge
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-edges.html#modify-an-edge">API Documentation</a>
     */
    <T> EdgeUpdateEntity updateEdge(String key, T value) ;

    /**
     * Partially updates the edge identified by document-key. The value must contain a document with the attributes to
     * patch (the patch document). All attributes from the patch document will be added to the existing document if they
     * do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key     The key of the edge
     * @param <T>     The type of the edge-document (POJO, {@link com.arangodb.util.RawJson} or {@link com.arangodb.util.RawBytes})
     * @param options Additional options, can be null
     * @return information about the edge
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-edges.html#modify-an-edge">API Documentation</a>
     */
    <T> EdgeUpdateEntity updateEdge(String key, T value, EdgeUpdateOptions options) ;

    /**
     * Removes a edge
     *
     * @param key The key of the edge
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-edges.html#remove-an-edge">API Documentation</a>
     */
    void deleteEdge(String key) ;

    /**
     * Removes a edge
     *
     * @param key     The key of the edge
     * @param options Additional options, can be null
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-edges.html#remove-an-edge">API Documentation</a>
     */
    void deleteEdge(String key, EdgeDeleteOptions options) ;

}
