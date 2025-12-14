package com.example;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.Config;
import org.junit.After;
import org.junit.Before;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

public class BaseTest {

    protected String createdCourierId;

    // Константы для базовых настроек
    protected static final int CONNECTION_TIMEOUT_MS = 30000;
    protected static final int SOCKET_TIMEOUT_MS = 30000;
    protected static final int PING_EXPECTED_STATUS = 200;
    protected static final int DELETE_EXPECTED_STATUS = 200;

    @Step("Настройка базового URL и фильтров")
    @Before
    public void setUp() {
        // Настройка базового URL
        RestAssured.baseURI = Config.BASE_URL;

        // Настройка фильтров для логирования и Allure
        RestAssured.filters(
                new RequestLoggingFilter(),
                new ResponseLoggingFilter(),
                new AllureRestAssured()
        );

        // Настройка таймаутов с использованием констант
        RestAssured.config = RestAssured.config()
                .httpClient(RestAssured.config().getHttpClientConfig()
                        .setParam("http.connection.timeout", CONNECTION_TIMEOUT_MS)
                        .setParam("http.socket.timeout", SOCKET_TIMEOUT_MS));

        // Проверка доступности сервера перед тестами
        checkServerAvailability();
    }

    @Step("Очистка тестовых данных")
    @After
    public void tearDown() {
        // Очистка созданных данных после каждого теста
        cleanupTestData();
    }

    @Step("Проверка доступности сервера")
    private void checkServerAvailability() {
        try {
            Response response = given()
                    .when()
                    .get(Config.PING);

            if (response.statusCode() != PING_EXPECTED_STATUS) {
                System.out.println("⚠️  Сервер недоступен или отвечает с ошибкой: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка подключения к серверу: " + e.getMessage());
        }
    }

    @Step("Создание курьера: логин = {courier.login}")
    protected String createCourier(TestData.Courier courier) {
        Response response = given()
                .contentType(ContentType.JSON)
                .body(courier)
                .when()
                .post(Config.CREATE_COURIER);

        if (response.statusCode() == 201) {
            return loginCourier(courier.getLogin(), courier.getPassword());
        }
        return null;
    }

    @Step("Авторизация курьера: логин = {login}")
    protected String loginCourier(String login, String password) {
        try {
            Response response = given()
                    .contentType(ContentType.JSON)
                    .body("{\"login\": \"" + login + "\", \"password\": \"" + password + "\"}")
                    .when()
                    .post(Config.LOGIN_COURIER);

            if (response.statusCode() == 200) {
                return response.jsonPath().getString("id");
            }
        } catch (Exception e) {
            System.out.println("⚠️  Ошибка авторизации курьера: " + e.getMessage());
        }
        return null;
    }

    @Step("Удаление курьера с ID = {id}")
    protected void deleteCourier(String id) {
        if (id != null && !id.isEmpty()) {
            try {
                given()
                        .contentType(ContentType.JSON)
                        .when()
                        .delete(Config.DELETE_COURIER + id)
                        .then()
                        .statusCode(DELETE_EXPECTED_STATUS);
            } catch (Exception e) {
                System.out.println("⚠️  Не удалось удалить курьера с ID: " + id);
            }
        }
    }

    @Step("Создание заказа")
    protected Response createOrder(TestData.Order order) {
        return given()
                .contentType(ContentType.JSON)
                .body(order)
                .when()
                .post(Config.CREATE_ORDER);
    }

    @Step("Получение заказа по треку: track = {trackNumber}")
    protected Response getOrderByTrackWithRetry(int trackNumber, int maxAttempts, int delaySeconds) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Response response = given()
                    .param("t", trackNumber)
                    .when()
                    .get(Config.GET_ORDER_BY_TRACK);

            if (response.statusCode() == 200) {
                return response;
            }

            if (attempt < maxAttempts) {
                try {
                    System.out.println("Попытка " + attempt + ": заказ не найден, ожидание " +
                            delaySeconds + " секунд...");
                    TimeUnit.SECONDS.sleep(delaySeconds);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return null;
    }

    @Step("Очистка тестовых данных")
    protected void cleanupTestData() {
        // Удаление созданного курьера
        if (createdCourierId != null) {
            deleteCourier(createdCourierId);
            createdCourierId = null;
        }
    }

    @Step("Проверка статус кода: ожидаем {expectedStatusCode}")
    protected void assertStatusCodeWithLogging(Response response, int expectedStatusCode, String testName) {
        int actualStatusCode = response.statusCode();

        if (actualStatusCode != expectedStatusCode) {
            System.out.println("Тест: " + testName);
            System.out.println("Ожидался статус: " + expectedStatusCode);
            System.out.println("Получен статус: " + actualStatusCode);
            System.out.println("Тело ответа: " + response.getBody().asString());

            // Для временных ошибок сервера
            if (actualStatusCode >= 500 && actualStatusCode < 600) {
                System.out.println("⚠️  Временная ошибка сервера, тест может быть пропущен");
            }
        }

        assertEquals("Неверный статус код в тесте: " + testName,
                expectedStatusCode, actualStatusCode);
    }

    @Step("Гибкая проверка кода ответа (допускает несколько вариантов)")
    protected void assertStatusCodeFlexible(Response response, int[] expectedStatusCodes, String testName) {
        int actualStatusCode = response.statusCode();
        boolean isValid = false;

        for (int expectedCode : expectedStatusCodes) {
            if (actualStatusCode == expectedCode) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            System.out.println("Тест: " + testName);
            System.out.println("Ожидался один из статусов: " + Arrays.toString(expectedStatusCodes));
            System.out.println("Получен статус: " + actualStatusCode);
            System.out.println("Тело ответа: " + response.getBody().asString());

            fail("Неверный статус код в тесте: " + testName +
                    ". Ожидался один из: " + Arrays.toString(expectedStatusCodes) +
                    ", получен: " + actualStatusCode);
        }
    }
}