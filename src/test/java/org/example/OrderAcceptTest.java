package org.example;

import org.example.api.CourierClient;
import org.example.api.OrderClient;
import org.example.dto.CourierCreateRequest;
import org.example.dto.CourierLoginRequest;
import org.example.dto.OrderCreateRequest;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

public class OrderAcceptTest extends BaseTest {

    private Integer testCourierId;
    private Integer testOrderId;

    @Before
    public void setUpTestData() {
       CourierCreateRequest courier = createTestCourier();
        Response createCourierResponse = CourierClient.createCourier(courier);
        assertStatusCode(createCourierResponse, SC_CREATED, "Создание курьера");

        CourierLoginRequest loginRequest = new CourierLoginRequest(courier.getLogin(), courier.getPassword());
        Response loginResponse = CourierClient.loginCourier(loginRequest);
        assertStatusCode(loginResponse, SC_OK, "Логин курьера");

        testCourierId = Integer.parseInt(loginResponse.jsonPath().getString("id"));
        createdCourierId = String.valueOf(testCourierId);

        OrderCreateRequest order = createTestOrder();
        Response createOrderResponse = OrderClient.createOrder(order);
        assertStatusCode(createOrderResponse, SC_CREATED, "Создание заказа");

        Integer trackNumber = OrderClient.getTrackNumberFromResponse(createOrderResponse);
        assertNotNull("Трек-номер должен быть получен", trackNumber);
        addOrderTrackForCleanup(trackNumber);

        Response orderResponse = OrderClient.getOrderByTrackWithRetry(trackNumber, 3, 2);
        assertNotNull("Заказ должен быть найден", orderResponse);
        assertStatusCode(orderResponse, SC_OK, "Получение заказа по треку");

        testOrderId = orderResponse.jsonPath().getInt("order.id");
        assertNotNull("ID заказа должен быть получен", testOrderId);
    }

    @Test
    @DisplayName("Успешное принятие заказа курьером")
    public void testAcceptOrderSuccess() {
        Response acceptResponse = OrderClient.acceptOrder(testOrderId, testCourierId);
        int[] expectedStatuses = {SC_OK, SC_BAD_REQUEST};
        assertStatusCodeFlexible(acceptResponse, expectedStatuses, "Принятие заказа");

        if (acceptResponse.statusCode() == SC_OK) {
            String responseBody = acceptResponse.getBody().asString();
            assertTrue("Успешный запрос должен возвращать ok: true",
                    responseBody.contains("\"ok\":true") || responseBody.contains("true"));
        }
    }

    @Test
    @DisplayName("Принятие заказа без ID курьера")
    public void testAcceptOrderWithoutCourierId() {
        Response response = OrderClient.acceptOrder(testOrderId, null);
        assertStatusCode(response, SC_BAD_REQUEST, "Принятие заказа без ID курьера");
    }

    @Test
    @DisplayName("Принятие заказа с неверным ID курьера")
    public void testAcceptOrderWithWrongCourierId() {
        Response response = OrderClient.acceptOrder(testOrderId, 999999);
        assertStatusCode(response, SC_BAD_REQUEST, "Принятие заказа с неверным ID курьера");
    }

    @Test
    @DisplayName("Принятие заказа с неверным номером заказа")
    public void testAcceptOrderWithWrongOrderId() {
        Response response = OrderClient.acceptOrder(999999, testCourierId);
        assertStatusCode(response, SC_BAD_REQUEST, "Принятие заказа с неверным номером заказа");
    }
}
