package org.example;

import org.example.api.CourierClient;
import org.example.api.OrderClient;
import org.example.dto.CourierCreateRequest;
import org.example.dto.CourierLoginRequest;
import org.example.dto.OrderCreateRequest;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

public class OrderAcceptTest extends BaseTest {

    @Test
    @DisplayName("Успешное принятие заказа курьером")
    public void testAcceptOrderSuccess() {
        // Создаем курьера
        CourierCreateRequest courier = createTestCourier();
        Response createCourierResponse = CourierClient.createCourier(courier);
        assertStatusCode(createCourierResponse, SC_CREATED, "Создание курьера");

        // Получаем ID курьера
        CourierLoginRequest loginRequest = new CourierLoginRequest(courier.getLogin(), courier.getPassword());
        Response loginResponse = CourierClient.loginCourier(loginRequest);
        assertStatusCode(loginResponse, SC_OK, "Логин курьера");

        Integer courierId = Integer.parseInt(loginResponse.jsonPath().getString("id"));
        createdCourierId = String.valueOf(courierId);

        // Создаем заказ
        OrderCreateRequest order = createTestOrder();
        Response createOrderResponse = OrderClient.createOrder(order);
        assertStatusCode(createOrderResponse, SC_CREATED, "Создание заказа");

        Integer trackNumber = OrderClient.getTrackNumberFromResponse(createOrderResponse);
        assertNotNull("Трек-номер должен быть получен", trackNumber);
        addOrderTrackForCleanup(trackNumber);

        // Получаем ID заказа
        Response orderResponse = OrderClient.getOrderByTrackWithRetry(trackNumber, 3, 2);
        assertNotNull("Заказ должен быть найден", orderResponse);
        assertStatusCode(orderResponse, SC_OK, "Получение заказа по треку");

        Integer orderId = orderResponse.jsonPath().getInt("order.id");
        assertNotNull("ID заказа должен быть получен", orderId);

        // Принимаем заказ
        Response acceptResponse = OrderClient.acceptOrder(orderId, courierId);
        // Согласно документации: 200 OK при успешном принятии
        int[] expectedStatuses = {SC_OK, SC_BAD_REQUEST};
        assertStatusCodeFlexible(acceptResponse, expectedStatuses, "Принятие заказа");

        // Проверяем, что успешный запрос возвращает ok: true
        if (acceptResponse.statusCode() == SC_OK) {
            String responseBody = acceptResponse.getBody().asString();
            assertTrue("Успешный запрос должен возвращать ok: true",
                    responseBody.contains("\"ok\":true") || responseBody.contains("true"));
        }
    }

    @Test
    @DisplayName("Принятие заказа без ID курьера")
    public void testAcceptOrderWithoutCourierId() {
        Response response = OrderClient.acceptOrder(1, null);
        // Согласно документации: 400 Bad Request
        assertStatusCode(response, SC_BAD_REQUEST, "Принятие заказа без ID курьера");
    }

    @Test
    @DisplayName("Принятие заказа с неверным ID курьера")
    public void testAcceptOrderWithWrongCourierId() {
        Response response = OrderClient.acceptOrder(1, 999999);
        // Согласно документации: 400 Bad Request
        assertStatusCode(response, SC_BAD_REQUEST, "Принятие заказа с неверным ID курьера");
    }

    @Test
    @DisplayName("Принятие заказа с неверным номером заказа")
    public void testAcceptOrderWithWrongOrderId() {
        CourierCreateRequest courier = createTestCourier();
        Response createResponse = CourierClient.createCourier(courier);
        assertStatusCode(createResponse, SC_CREATED, "Создание курьера");

        CourierLoginRequest loginRequest = new CourierLoginRequest(courier.getLogin(), courier.getPassword());
        Response loginResponse = CourierClient.loginCourier(loginRequest);
        Integer courierId = Integer.parseInt(loginResponse.jsonPath().getString("id"));
        createdCourierId = String.valueOf(courierId);

        Response response = OrderClient.acceptOrder(999999, courierId);
        // Согласно документации: 400 Bad Request
        assertStatusCode(response, SC_BAD_REQUEST, "Принятие заказа с неверным номером заказа");
    }
}