package org.example;

import org.example.api.OrderClient;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class OrderListTest extends BaseTest {

    @Test
    @DisplayName("Получение списка заказов")
    public void testGetOrdersList() {
        Response response = OrderClient.getOrdersList(null, null);

        response.then()
                .statusCode(SC_OK)
                .body("orders", notNullValue());
    }

    @Test
    @DisplayName("Получение списка заказов с лимитом")
    public void testGetOrdersListWithLimit() {
        Response response = OrderClient.getOrdersList(5, null);

        response.then()
                .statusCode(SC_OK)
                .body("orders", notNullValue());
    }

    @Test
    @DisplayName("Получение списка заказов со станциями метро")
    public void testGetOrdersListWithStations() {
        Response response = OrderClient.getOrdersList(null, "[\"1\", \"2\"]");

        response.then()
                .statusCode(SC_OK)
                .body("orders", notNullValue());
    }
}