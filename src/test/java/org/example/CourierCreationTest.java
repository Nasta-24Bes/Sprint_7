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

        // Проверяем, что успешный запрос возвращает ok: true
        String responseBody = createResponse.getBody().asString();
        assertTrue("Успешный запрос должен возвращать ok: true",
                responseBody.contains("\"ok\":true") || responseBody.contains("true"));

        // Авторизуемся для получения ID
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

        // Сохраняем ID для очистки
        CourierLoginRequest loginRequest = new CourierLoginRequest(courier.getLogin(), courier.getPassword());
        Response loginResponse = CourierClient.loginCourier(loginRequest);
        saveCourierId(loginResponse);
    }

    @Test
    @DisplayName("Создание курьера без логина")
    public void testCreateCourierWithoutLogin() {
        CourierCreateRequest courier = new CourierCreateRequest(null, "password123", "TestName");

        Response response = CourierClient.createCourier(courier);
        // Согласно документации: 400 Bad Request
        assertStatusCode(response, SC_BAD_REQUEST, "Создание курьера без логина");

        String errorMessage = response.jsonPath().getString("message");
        assertNotNull("Должно быть сообщение об ошибке при отсутствии логина", errorMessage);
    }

    @Test
    @DisplayName("Создание курьера без пароля")
    public void testCreateCourierWithoutPassword() {
        CourierCreateRequest courier = new CourierCreateRequest("testlogin", null, "TestName");

        Response response = CourierClient.createCourier(courier);
        // Согласно документации: 400 Bad Request
        assertStatusCode(response, SC_BAD_REQUEST, "Создание курьера без пароля");

        String errorMessage = response.jsonPath().getString("message");
        assertNotNull("Должно быть сообщение об ошибке при отсутствии пароля", errorMessage);
    }

    @Test
    @DisplayName("Создание курьера без имени")
    public void testCreateCourierWithoutFirstName() {
        CourierCreateRequest courier = new CourierCreateRequest("testlogin", "password123", null);

        Response response = CourierClient.createCourier(courier);
        // Согласно документации: firstName не обязательное поле
        // Ожидаем либо 201 Created, либо 409 если такой логин уже есть
        int[] expectedStatuses = {SC_CREATED, SC_CONFLICT};
        assertStatusCodeFlexible(response, expectedStatuses, "Создание курьера без имени");

        // Если создался успешно - сохраняем ID для очистки
        if (response.statusCode() == SC_CREATED) {
            CourierLoginRequest loginRequest = new CourierLoginRequest(courier.getLogin(), courier.getPassword());
            Response loginResponse = CourierClient.loginCourier(loginRequest);
            saveCourierId(loginResponse);
        }
    }
}