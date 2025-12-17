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

public class CourierLoginTest extends BaseTest {

    private CourierCreateRequest testCourier;
    private String testCourierLogin;
    private String testCourierPassword;

    @Before
    public void setUpTestCourier() {
        testCourier = createTestCourier();
        testCourierLogin = testCourier.getLogin();
        testCourierPassword = testCourier.getPassword();

        Response createResponse = CourierClient.createCourier(testCourier);
        if (createResponse.statusCode() == SC_CREATED) {
            CourierLoginRequest loginRequest = new CourierLoginRequest(testCourierLogin, testCourierPassword);
            Response loginResponse = CourierClient.loginCourier(loginRequest);
            saveCourierId(loginResponse);
        }
    }

    @Test
    @DisplayName("Успешная авторизация курьера")
    public void testLoginCourierSuccess() {
        CourierLoginRequest loginRequest = new CourierLoginRequest(testCourierLogin, testCourierPassword);
        Response loginResponse = CourierClient.loginCourier(loginRequest);

        assertStatusCode(loginResponse, SC_OK, "Авторизация курьера");

        String courierId = loginResponse.jsonPath().getString("id");
        assertNotNull("ID курьера должен быть в ответе", courierId);
    }

    @Test
    @DisplayName("Авторизация с неверным паролем")
    public void testLoginWithWrongPassword() {
        CourierLoginRequest wrongPassword = new CourierLoginRequest(testCourierLogin, "wrongpassword");
        Response wrongPasswordResponse = CourierClient.loginCourier(wrongPassword);

        assertStatusCode(wrongPasswordResponse, SC_NOT_FOUND, "Авторизация с неверным паролем");

        String errorMessage = wrongPasswordResponse.jsonPath().getString("message");
        assertNotNull("Должно быть сообщение об ошибке при неверном пароле", errorMessage);
        assertEquals("Неверный текст ошибки при авторизации с неверным паролем",
                "Учетная запись не найдена", errorMessage);
    }

    @Test
    @DisplayName("Авторизация несуществующего курьера")
    public void testLoginNonExistentCourier() {
        CourierLoginRequest nonExistent = new CourierLoginRequest("nonexistent" + System.currentTimeMillis(), "password");
        Response response = CourierClient.loginCourier(nonExistent);

        assertStatusCode(response, SC_NOT_FOUND, "Авторизация несуществующего курьера");

        String errorMessage = response.jsonPath().getString("message");
        assertNotNull("Должно быть сообщение об ошибке при несуществующем курьере", errorMessage);
        assertEquals("Неверный текст ошибки при авторизации несуществующего курьера",
                "Учетная запись не найдена", errorMessage);
    }

    @Test
    @DisplayName("Авторизация без логина")
    public void testLoginWithoutLogin() {
        CourierLoginRequest noLogin = new CourierLoginRequest(null, "password123");
        Response response = CourierClient.loginCourier(noLogin);

        assertStatusCode(response, SC_BAD_REQUEST, "Авторизация без логина");

        String errorMessage = response.jsonPath().getString("message");
        assertNotNull("Должно быть сообщение об ошибке при отсутствии логина", errorMessage);
        assertEquals("Неверный текст ошибки при авторизации без логина",
                "Недостаточно данных для входа", errorMessage);
    }

    @Test
    @DisplayName("Авторизация без пароля")
    public void testLoginWithoutPassword() {
        CourierLoginRequest noPassword = new CourierLoginRequest(testCourierLogin, null);
        Response noPasswordResponse = CourierClient.loginCourier(noPassword);

        int[] expectedStatuses = {SC_BAD_REQUEST};
        assertStatusCodeFlexible(noPasswordResponse, expectedStatuses, "Авторизация без пароля");

        // Если API вернул 400 - проверяем сообщение об ошибке
        if (noPasswordResponse.statusCode() == SC_BAD_REQUEST) {
            String errorMessage = noPasswordResponse.jsonPath().getString("message");
            assertNotNull("Должно быть сообщение об ошибке при отсутствии пароля", errorMessage);
            assertEquals("Неверный текст ошибки при авторизации без пароля",
                    "Недостаточно данных для входа", errorMessage);
        }
    }
}
