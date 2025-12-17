package org.example.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.example.Config;

import static io.restassured.RestAssured.given;

public class BaseClient {

    protected static RequestSpecification getBaseSpec() {
        return given()
                .baseUri(Config.BASE_URL)
                .contentType(ContentType.JSON);
    }

    protected static Response post(String path, Object body) {
        return getBaseSpec()
                .body(body)
                .when()
                .post(path);
    }

    protected static Response put(String path, Object body) {
        return getBaseSpec()
                .body(body)
                .when()
                .put(path);
    }

    protected static Response delete(String path) {
        return getBaseSpec()
                .when()
                .delete(path);
    }
}
