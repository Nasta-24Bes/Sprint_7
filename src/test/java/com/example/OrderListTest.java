package com.example;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.example.Config;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderListTest extends BaseTest {

    @Test
    @DisplayName("Получение списка заказов")
    public void testGetOrdersList() {
        Response response = given()
                .when()
                .get(Config.GET_ORDERS);

        response.then()
                .statusCode(200)
                .body("orders", notNullValue());
    }

    @Test
    @DisplayName("Получение списка заказов с лимитом")
    public void testGetOrdersListWithLimit() {
        Response response = given()
                .param("limit", 5)
                .when()
                .get(Config.GET_ORDERS);

        response.then()
                .statusCode(200)
                .body("orders", notNullValue());
    }

    @Test
    @DisplayName("Получение списка заказов со станциями метро")
    public void testGetOrdersListWithStations() {
        Response response = given()
                .param("nearestStation", "[\"1\", \"2\"]")
                .when()
                .get(Config.GET_ORDERS);

        response.then()
                .statusCode(200)
                .body("orders", notNullValue());
    }

    @Test
    @DisplayName("Получение списка заказов без параметров")
    public void testGetOrdersListDefault() {
        Response response = given()
                .when()
                .get(Config.GET_ORDERS);

        response.then()
                .statusCode(200)
                .body("pageInfo", notNullValue())
                .body("orders", notNullValue());
    }
}