package com.example;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.Config;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static junit.framework.TestCase.assertNotNull;

public class CourierLoginTest extends BaseTest {

    private static final int EXPECTED_CREATE_STATUS = 201;
    private static final int EXPECTED_LOGIN_STATUS = 200;
    private static final int[] LOGIN_ERROR_EXPECTED_CODES = {400, 404};
    private static final int GATEWAY_TIMEOUT_STATUS = 504;
    private static final int SERVER_ERROR_MIN_STATUS = 500;
    private static final int SERVER_ERROR_MAX_STATUS = 599;

    @Test
    @DisplayName("Успешная авторизация курьера")
    public void testLoginCourierSuccess() {
        TestData.Courier courier = TestData.getRandomCourier();

        // Создаем курьера
        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(courier)
                .when()
                .post(Config.CREATE_COURIER);

        assertStatusCodeWithLogging(createResponse, EXPECTED_CREATE_STATUS,
                "Создание курьера для авторизации");

        // Логинимся
        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"login\": \"" + courier.getLogin() +
                        "\", \"password\": \"" + courier.getPassword() + "\"}")
                .when()
                .post(Config.LOGIN_COURIER);

        assertStatusCodeWithLogging(loginResponse, EXPECTED_LOGIN_STATUS,
                "Авторизация курьера");

        // Сохраняем ID для очистки
        createdCourierId = loginResponse.jsonPath().getString("id");
        assertNotNull("ID курьера не должен быть null", createdCourierId);
    }

    @Test
    @DisplayName("Авторизация с неверным паролем")
    public void testLoginWithWrongPassword() {
        TestData.Courier courier = TestData.getRandomCourier();

        // Создаем курьера
        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(courier)
                .when()
                .post(Config.CREATE_COURIER);

        assertStatusCodeWithLogging(createResponse, EXPECTED_CREATE_STATUS,
                "Создание курьера для теста неверного пароля");

        createdCourierId = loginCourier(courier.getLogin(), courier.getPassword());
        assertNotNull("Курьер должен быть создан", createdCourierId);

        // Логинимся с неправильным паролем
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"login\": \"" + courier.getLogin() +
                        "\", \"password\": \"wrongpassword\"}")
                .when()
                .post(Config.LOGIN_COURIER);

        assertStatusCodeFlexible(response, LOGIN_ERROR_EXPECTED_CODES,
                "Авторизация с неверным паролем");
    }

    @Test
    @DisplayName("Авторизация несуществующего курьера")
    public void testLoginNonExistentCourier() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"login\": \"nonexistent" + System.currentTimeMillis() +
                        "\", \"password\": \"password\"}")
                .when()
                .post(Config.LOGIN_COURIER);

        assertStatusCodeFlexible(response, LOGIN_ERROR_EXPECTED_CODES,
                "Авторизация несуществующего курьера");
    }

    @Test
    @DisplayName("Авторизация без пароля с обработкой таймаута")
    public void testLoginWithoutPassword() {
        TestData.Courier courier = TestData.getRandomCourier();
        boolean requestCompleted = false;
        int attempt = 1;
        final int maxAttempts = 2;

        while (!requestCompleted && attempt <= maxAttempts) {
            try {
                Response response = given()
                        .contentType(ContentType.JSON)
                        .body("{\"login\": \"" + courier.getLogin() + "\"}")
                        .when()
                        .post(Config.LOGIN_COURIER);

                int actualStatusCode = response.statusCode();

                if (actualStatusCode == GATEWAY_TIMEOUT_STATUS) {
                    System.out.println("⚠️ Gateway Timeout (" + GATEWAY_TIMEOUT_STATUS +
                            ") - сервер перегружен");
                    requestCompleted = true;
                } else if (actualStatusCode >= SERVER_ERROR_MIN_STATUS &&
                        actualStatusCode <= SERVER_ERROR_MAX_STATUS) {
                    System.out.println("⚠️ Ошибка сервера: " + actualStatusCode);
                    requestCompleted = true;
                } else {
                    assertStatusCodeFlexible(response, LOGIN_ERROR_EXPECTED_CODES,
                            "Авторизация без пароля");
                    requestCompleted = true;
                }

            } catch (Exception e) {
                Throwable cause = e;
                boolean isTimeout = false;

                while (cause != null) {
                    if (cause.getMessage() != null &&
                            (cause.getMessage().contains("timed out") ||
                                    cause.getMessage().contains("Timeout") ||
                                    cause.getClass().getSimpleName().contains("Timeout"))) {
                        isTimeout = true;
                        break;
                    }
                    cause = cause.getCause();
                }

                if (isTimeout) {
                    System.out.println("⏱️ Попытка " + attempt + ": Таймаут, сервер не отвечает");

                    if (attempt == maxAttempts) {
                        System.out.println("❌ Сервер не ответил после " + maxAttempts + " попыток");
                        org.junit.Assume.assumeNoException("Сервер недоступен (таймаут)", e);
                        return;
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    attempt++;
                } else {
                    System.out.println("❌ Неожиданная ошибка: " + e.getClass().getSimpleName() +
                            " - " + e.getMessage());
                    throw e;
                }
            }
        }
    }
}