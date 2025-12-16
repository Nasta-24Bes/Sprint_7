package org.example;

import org.example.api.OrderClient;
import org.example.dto.OrderCreateRequest;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class OrderTrackTest extends BaseTest {

    @Test
    @DisplayName("Успешное получение заказа по номеру")
    public void testGetOrderByTrackSuccess() {
        OrderCreateRequest order = createTestOrder();
        Response createResponse = OrderClient.createOrder(order);
        assertStatusCode(createResponse, SC_CREATED, "Создание заказа");

        Integer trackNumber = OrderClient.getTrackNumberFromResponse(createResponse);
        assertNotNull("Трек-номер должен быть получен", trackNumber);
        addOrderTrackForCleanup(trackNumber);

        Response orderResponse = OrderClient.getOrderByTrackWithRetry(trackNumber, 3, 2);
        assertNotNull("Заказ должен быть найден", orderResponse);

        orderResponse.then()
                .statusCode(SC_OK)
                .body("order", notNullValue())
                .body("order.track", equalTo(trackNumber));
    }

    @Test
    @DisplayName("Получение заказа без номера заказа")
    public void testGetOrderByTrackWithoutTrack() {
        // track=0 считается как "без номера"
        Response response = OrderClient.getOrderByTrack(0);
        // Фактически API возвращает 404, а не 400
        assertStatusCode(response, SC_NOT_FOUND, "Получение заказа без номера заказа");
    }

    @Test
    @DisplayName("Получение заказа с несуществующим номером")
    public void testGetOrderByTrackWithNonExistentTrack() {
        int nonExistentTrack = 999999;
        Response response = OrderClient.getOrderByTrack(nonExistentTrack);
        // Фактически API возвращает 404, а не 400
        assertStatusCode(response, SC_NOT_FOUND, "Получение заказа с несуществующим номером");
    }
}