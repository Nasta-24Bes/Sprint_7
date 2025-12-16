package org.example;

import org.example.api.CourierClient;
import org.example.api.OrderClient;
import org.example.dto.CourierCreateRequest;
import org.example.dto.OrderCreateRequest;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BaseTest {

    protected String createdCourierId;
    protected List<Integer> createdOrderTracks = new ArrayList<>();

    @Step("Настройка базового URL и фильтров")
    @Before
    public void setUp() {
        RestAssured.baseURI = Config.BASE_URL;
        RestAssured.filters(
                new RequestLoggingFilter(),
                new ResponseLoggingFilter(),
                new AllureRestAssured()
        );
        checkServerAvailability();
    }

    @Step("Очистка тестовых данных")
    @After
    public void tearDown() {
        cleanupTestData();
    }

    @Step("Проверка доступности сервера")
    private void checkServerAvailability() {
        try {
            Response response = RestAssured.given()
                    .when()
                    .get(Config.PING);

            if (response.statusCode() != SC_OK) {
                System.out.println("⚠️ Сервер недоступен: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка подключения к серверу: " + e.getMessage());
        }
    }

    @Step("Создание тестового курьера")
    protected CourierCreateRequest createTestCourier() {
        String uniqueLogin = "courier_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
        return new CourierCreateRequest(uniqueLogin, "password123", "TestName");
    }

    @Step("Создание тестового заказа")
    protected OrderCreateRequest createTestOrder() {
        return new OrderCreateRequest(
                "Naruto",
                "Uzumaki",
                "Konoha, 142 apt.",
                "4",
                "+7 800 355 35 35",
                5,
                "2023-12-31",
                "Test comment",
                java.util.Collections.singletonList("BLACK")
        );
    }

    @Step("Создание тестового заказа с указанными цветами")
    protected OrderCreateRequest createTestOrderWithColors(List<String> colors) {
        return new OrderCreateRequest(
                "Naruto",
                "Uzumaki",
                "Konoha, 142 apt.",
                "4",
                "+7 800 355 35 35",
                5,
                "2023-12-31",
                "Test comment",
                colors
        );
    }

    @Step("Очистка тестовых данных")
    protected void cleanupTestData() {
        // Отмена заказов
        for (Integer track : createdOrderTracks) {
            try {
                Response cancelResponse = OrderClient.cancelOrder(track);
                if (cancelResponse.statusCode() != SC_OK) {
                    System.out.println("⚠️ Не удалось отменить заказ: " + track);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Ошибка при отмене заказа: " + track);
            }
        }
        createdOrderTracks.clear();

        // Удаление курьера
        if (createdCourierId != null) {
            try {
                Response deleteResponse = CourierClient.deleteCourier(createdCourierId);
                if (deleteResponse.statusCode() != SC_OK) {
                    System.out.println("⚠️ Не удалось удалить курьера: " + deleteResponse.statusCode());
                }
            } catch (Exception e) {
                System.out.println("⚠️ Ошибка при удалении курьера: " + e.getMessage());
            }
            createdCourierId = null;
        }
    }

    @Step("Проверка статус кода")
    protected void assertStatusCode(Response response, int expectedStatusCode, String message) {
        assertEquals(message, expectedStatusCode, response.statusCode());
    }

    @Step("Гибкая проверка статус кода")
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
            System.out.println("Ожидался один из: " + java.util.Arrays.toString(expectedStatusCodes));
            System.out.println("Получен: " + actualStatusCode);
            System.out.println("Тело: " + response.getBody().asString());

            fail("Неверный статус код в тесте: " + testName +
                    ". Ожидался один из: " + java.util.Arrays.toString(expectedStatusCodes) +
                    ", получен: " + actualStatusCode);
        }
    }

    @Step("Добавление трека заказа для последующей отмены")
    protected void addOrderTrackForCleanup(Integer track) {
        if (track != null) {
            createdOrderTracks.add(track);
        }
    }

    @Step("Сохранение ID курьера после авторизации")
    protected void saveCourierId(Response loginResponse) {
        if (loginResponse.statusCode() == SC_OK) {
            createdCourierId = loginResponse.jsonPath().getString("id");
        }
    }
}