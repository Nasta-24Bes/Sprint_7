package org.example;

import org.example.api.CourierClient;
import org.example.dto.CourierCreateRequest;
import org.example.dto.CourierLoginRequest;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

public class CourierCreationTest extends BaseTest {

    @Test
    @DisplayName("Успешное создание курьера")
    public void testCreateCourierSuccess() {
        CourierCreateRequest courier = createTestCourier();

        Response createResponse = CourierClient.createCourier(courier);
        assertStatusCode(createResponse, SC_CREATED, "Создание курьера");

        String responseBody = createResponse.getBody().asString();
        assertTrue("Успешный запрос должен возвращать ok: true",
                responseBody.contains("\"ok\":true") || responseBody.contains("true"));

        CourierLoginRequest loginRequest = new CourierLoginRequest(courier.getLogin(), courier.getPassword());
        Response loginResponse = CourierClient.loginCourier(loginRequest);

        saveCourierId(loginResponse);
        assertNotNull("ID курьера должен быть получен", createdCourierId);
    }

    @Test
    @DisplayName("Создание дубликата курьера")
    public void testCreateDuplicateCourier() {
        CourierCreateRequest courier = createTestCourier();

        // Первое создание
        Response firstCreate = CourierClient.createCourier(courier);
        assertStatusCode(firstCreate, SC_CREATED, "Первое создание курьера");

        // Второе создание (дубликат) - ожидаем 409 CONFLICT
        Response secondCreate = CourierClient.createCourier(courier);
        assertStatusCode(secondCreate, SC_CONFLICT, "Создание дубликата курьера");

        // Проверяем сообщение об ошибке
        String errorMessage = secondCreate.jsonPath().getString("message");
        assertNotNull("Должно быть сообщение об ошибке при дублировании", errorMessage);
        assertEquals("Неверный текст ошибки при создании дубликата курьера", 
                "Этот логин уже используется. Попробуйте другой.", errorMessage);
    }

    @Test
    @DisplayName("Создание курьера без логина")
    public void testCreateCourierWithoutLogin() {
        CourierCreateRequest courier = new CourierCreateRequest(null, "password123", "TestName");

        Response response = CourierClient.createCourier(courier);
        assertStatusCode(response, SC_BAD_REQUEST, "Создание курьера без логина");

        // Проверяем сообщение об ошибке
        String errorMessage = response.jsonPath().getString("message");
        assertNotNull("Должно быть сообщение об ошибке при отсутствии логина", errorMessage);
        assertEquals("Неверный текст ошибки при создании курьера без логина", 
                "Недостаточно данных для создания учетной записи", errorMessage);
    }

    @Test
    @DisplayName("Создание курьера без пароля")
    public void testCreateCourierWithoutPassword() {
        CourierCreateRequest courier = new CourierCreateRequest("testlogin", null, "TestName");

        Response response = CourierClient.createCourier(courier);
        assertStatusCode(response, SC_BAD_REQUEST, "Создание курьера без пароля");

        // Проверяем сообщение об ошибке
        String errorMessage = response.jsonPath().getString("message");
        assertNotNull("Должно быть сообщение об ошибке при отсутствии пароля", errorMessage);
        assertEquals("Неверный текст ошибки при создании курьера без пароля", 
                "Недостаточно данных для создания учетной записи", errorMessage);
    }

    @Test
    @DisplayName("Создание курьера без имени")
    public void testCreateCourierWithoutFirstName() {
        CourierCreateRequest courier = new CourierCreateRequest("testlogin", "password123", null);

        Response response = CourierClient.createCourier(courier);
        int[] expectedStatuses = {SC_CREATED, SC_CONFLICT};
        assertStatusCodeFlexible(response, expectedStatuses, "Создание курьера без имени");

        // Если курьер создался с ошибкой - проверяем сообщение
        if (response.statusCode() == SC_CONFLICT) {
            String errorMessage = response.jsonPath().getString("message");
            assertNotNull("Должно быть сообщение об ошибке", errorMessage);
            assertEquals("Неверный текст ошибки при создании дубликата курьера без имени", 
                    "Этот логин уже используется. Попробуйте другой.", errorMessage);
        }
    }
}
