package org.example;

import org.example.api.OrderClient;
import org.example.dto.OrderCreateRequest;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class OrderTrackTest extends BaseTest {

    private Integer testTrackNumber;

    @Before
    public void setUpTestOrder() {
        OrderCreateRequest order = createTestOrder();
        Response createResponse = OrderClient.createOrder(order);
        assertStatusCode(createResponse, SC_CREATED, "Создание заказа");

        testTrackNumber = OrderClient.getTrackNumberFromResponse(createResponse);
        assertNotNull("Трек-номер должен быть получен", testTrackNumber);
        addOrderTrackForCleanup(testTrackNumber);
    }

    @Test
    @DisplayName("Успешное получение заказа по номеру")
    public void testGetOrderByTrackSuccess() {
        Response orderResponse = OrderClient.getOrderByTrackWithRetry(testTrackNumber, 3, 2);
        assertNotNull("Заказ должен быть найден", orderResponse);

        orderResponse.then()
                .statusCode(SC_OK)
                .body("order", notNullValue())
                .body("order.track", equalTo(testTrackNumber));
    }

    @Test
    @DisplayName("Получение заказа без номера заказа")
    public void testGetOrderByTrackWithoutTrack() {
        Response response = OrderClient.getOrderByTrack(0);
        assertStatusCode(response, SC_BAD_REQUEST, "Получение заказа без номера заказа");

        String errorMessage = response.jsonPath().getString("message");
        assertNotNull("Должно быть сообщение об ошибке", errorMessage);
        assertEquals("Неверный текст ошибки при получении заказа без номера",
                "Недостаточно данных для поиска", errorMessage);
    }

    @Test
    @DisplayName("Получение заказа с несуществующим номером")
    public void testGetOrderByTrackWithNonExistentTrack() {
        int nonExistentTrack = 999999;
        Response response = OrderClient.getOrderByTrack(nonExistentTrack);
        assertStatusCode(response, SC_BAD_REQUEST, "Получение заказа с несуществующим номером");

        String errorMessage = response.jsonPath().getString("message");
        assertNotNull("Должно быть сообщение об ошибке", errorMessage);
        assertEquals("Неверный текст ошибки при получении несуществующего заказа",
                "Недостаточно данных для поиска", errorMessage);
    }
}
