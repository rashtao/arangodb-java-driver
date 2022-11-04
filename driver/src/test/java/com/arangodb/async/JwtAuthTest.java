package com.arangodb.async;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.DbName;
import com.arangodb.internal.config.FileConfigPropertiesProvider;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.Request;
import com.arangodb.RequestType;
import com.arangodb.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class JwtAuthTest {

    private static String jwt;
    private ArangoDBAsync arangoDB;

    @BeforeAll
    static void init() {
        ArangoDB arangoDB = new ArangoDB.Builder()
                .loadProperties(new FileConfigPropertiesProvider())
                .build();
        jwt = getJwt(arangoDB);
        arangoDB.shutdown();
    }

    private static String getJwt(ArangoDB arangoDB) {
        ArangoSerde serde = arangoDB.getSerde();
        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("username", "root");
        reqBody.put("password", "test");

        Request req = new Request(DbName.SYSTEM, RequestType.POST, "/_open/auth");
        req.setBody(serde.serialize(reqBody));

        Response resp = arangoDB.execute(req);
        Map<String, String> respBody = serde.deserialize(resp.getBody(), Map.class);
        return respBody.get("jwt");
    }

    @AfterEach
    void after() {
        if (arangoDB != null)
            arangoDB.shutdown();
    }

    @Test
    void notAuthenticated() throws InterruptedException {
        arangoDB = getBuilder().build();
        try {
            arangoDB.getVersion().get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
            assertThat(((ArangoDBException) e.getCause()).getResponseCode()).isEqualTo(401);
        }
        arangoDB.shutdown();
    }

    @Test
    void authenticated() throws ExecutionException, InterruptedException {
        arangoDB = getBuilder()
                .jwt(jwt)
                .build();
        arangoDB.getVersion().get();
        arangoDB.shutdown();
    }

    @Test
    @Disabled("DE-423")
    void updateJwt() throws ExecutionException, InterruptedException {
        arangoDB = getBuilder()
                .jwt(jwt)
                .build();
        arangoDB.getVersion().get();
        arangoDB.updateJwt("bla");
        try {
            arangoDB.getVersion().get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
            assertThat(((ArangoDBException) e.getCause()).getResponseCode()).isEqualTo(401);
        }

        arangoDB.updateJwt(jwt);
        arangoDB.getVersion().get();
        arangoDB.shutdown();
    }

    private ArangoDBAsync.Builder getBuilder() {
        return new ArangoDBAsync.Builder()
                .loadProperties(new FileConfigPropertiesProvider())
                .jwt(null)          // unset credentials from properties file
                .user(null)         // unset credentials from properties file
                .password(null);    // unset credentials from properties file
    }
}