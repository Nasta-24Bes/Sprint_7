package org.example;

import org.example.api.CourierClient;
import org.example.dto.CourierCreateRequest;
import org.example.dto.CourierLoginRequest;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

public class CourierDeleteTest extends BaseTest {

    private String testCourierId;

    @Before
    public void setUpTestCourier() {
        CourierCreateRequest courier = createTestCourier();
        Response createResponse = CourierClient.createCourier(courier);
        assertStatusCode(createResponse, SC_CREATED, "Создание курьера");

        CourierLoginRequest loginRequest = new CourierLoginRequest(courier.getLogin(), courier.getPassword());
        Response loginResponse = CourierClient.loginCourier(loginRequest);
        testCourierId = loginResponse.jsonPath().getString("id");
        
        createdCourierId = testCourierId;
    }

    @Test
    @DisplayName("Успешное удаление курьера")
    public void testDeleteCourierSuccess() {
        Response deleteResponse = CourierClient.deleteCourier(testCourierId);
        assertStatusCode(deleteResponse, SC_OK, "Удаление курьера");

        String responseBody = deleteResponse.getBody().asString();
        assertTrue("Успешный запрос должен возвращать ok: true",
                responseBody.contains("\"ok\":true") || responseBody.contains("true"));
        
        createdCourierId = null;
    }

    @Test
    @DisplayName("Удаление курьера без ID")
    public void testDeleteCourierWithoutId() {
        Response response = CourierClient.deleteCourier("");
        assertStatusCode(response, SC_NOT_FOUND, "Удаление курьера без ID");

        // Проверяем сообщение об ошибке
        String errorMessage = response.jsonPath().getString("message");
        assertNotNull("Должно быть сообщение об ошибке при удалении без ID", errorMessage);
        assertEquals("Неверный текст ошибки при удалении курьера без ID",
                "Курьер с идентификатором  не найден", errorMessage);
    }

    @Test
    @DisplayName("Удаление курьера с несуществующим ID")
    public void testDeleteCourierWithNonExistentId() {
        String nonExistentId = "999999";
        Response response = CourierClient.deleteCourier(nonExistentId);
        assertStatusCode(response, SC_NOT_FOUND, "Удаление курьера с несуществующим ID");

        // Проверяем сообщение об ошибке
        String errorMessage = response.jsonPath().getString("message");
        assertNotNull("Должно быть сообщение об ошибке при удалении с несуществующим ID", errorMessage);
        assertEquals("Неверный текст ошибки при удалении курьера с несуществующим ID",
                "Курьер с идентификатором 999999 не найден", errorMessage);
    }
}
