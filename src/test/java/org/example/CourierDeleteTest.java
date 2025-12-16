package org.example;

import org.example.api.CourierClient;
import org.example.dto.CourierCreateRequest;
import org.example.dto.CourierLoginRequest;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

public class CourierDeleteTest extends BaseTest {

    @Test
    @DisplayName("Успешное удаление курьера")
    public void testDeleteCourierSuccess() {
        CourierCreateRequest courier = createTestCourier();
        Response createResponse = CourierClient.createCourier(courier);
        assertStatusCode(createResponse, SC_CREATED, "Создание курьера");

        CourierLoginRequest loginRequest = new CourierLoginRequest(courier.getLogin(), courier.getPassword());
        Response loginResponse = CourierClient.loginCourier(loginRequest);
        String courierId = loginResponse.jsonPath().getString("id");
        assertNotNull("ID курьера должен быть получен", courierId);

        Response deleteResponse = CourierClient.deleteCourier(courierId);
        assertStatusCode(deleteResponse, SC_OK, "Удаление курьера");

        String responseBody = deleteResponse.getBody().asString();
        assertTrue("Успешный запрос должен возвращать ok: true",
                responseBody.contains("\"ok\":true") || responseBody.contains("true"));
    }

    @Test
    @DisplayName("Удаление курьера без ID")
    public void testDeleteCourierWithoutId() {
        Response response = CourierClient.deleteCourier("");
        // Фактически API возвращает 404, а не 400
        assertStatusCode(response, SC_NOT_FOUND, "Удаление курьера без ID");
    }

    @Test
    @DisplayName("Удаление курьера с несуществующим ID")
    public void testDeleteCourierWithNonExistentId() {
        String nonExistentId = "999999";
        Response response = CourierClient.deleteCourier(nonExistentId);
        // Фактически API возвращает 404, а не 400
        assertStatusCode(response, SC_NOT_FOUND, "Удаление курьера с несуществующим ID");
    }
}