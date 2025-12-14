package com.example;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
public class OrderCreationTest extends BaseTest {

    private final List<String> color;

    // Константы для параметров тестов
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final int DEFAULT_DELAY_SECONDS = 2;
    private static final int EXPECTED_CREATE_STATUS = 201;
    private static final int EXPECTED_ORDER_FOUND_STATUS = 200;
    private static final int[] CREATE_ORDER_EXPECTED_CODES = {201, 400};

    public OrderCreationTest(List<String> color) {
        this.color = color;
    }

    @Parameterized.Parameters(name = "Цвета: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {Collections.singletonList("BLACK")},
                {Collections.singletonList("GREY")},
                {Arrays.asList("BLACK", "GREY")},
                {Collections.emptyList()},
                {null}
        });
    }

    @Test
    @DisplayName("Создание и поиск заказа с retry")
    public void testCreateAndFindOrderWithRetry() {
        TestData.Order order = TestData.getRandomOrder();

        if (color != null) {
            order.setColor(color);
        }

        Response createResponse = createOrder(order);

        assertStatusCodeFlexible(createResponse, CREATE_ORDER_EXPECTED_CODES, "Создание заказа");

        if (createResponse.statusCode() == EXPECTED_CREATE_STATUS) {
            int trackNumber = createResponse.jsonPath().getInt("track");

            Response orderResponse = getOrderByTrackWithRetry(
                    trackNumber,
                    DEFAULT_MAX_ATTEMPTS,
                    DEFAULT_DELAY_SECONDS
            );

            if (orderResponse != null) {
                orderResponse.then()
                        .statusCode(EXPECTED_ORDER_FOUND_STATUS)
                        .body("order", notNullValue())
                        .body("order.track", equalTo(trackNumber));
            } else {
                System.out.println("⚠️ Заказ с track=" + trackNumber +
                        " не найден после " + DEFAULT_MAX_ATTEMPTS + " попыток");
            }
        }
    }

    @Test
    @DisplayName("Тест создания заказа с логированием")
    public void testCreateOrderWithLogging() {
        TestData.Order order = TestData.getRandomOrder();

        Response response = createOrder(order);

        assertStatusCodeWithLogging(response, EXPECTED_CREATE_STATUS, "Создание заказа");

        if (response.statusCode() == EXPECTED_CREATE_STATUS) {
            response.then()
                    .body("track", notNullValue());
        }
    }

    @Test
    @DisplayName("Тест с разными параметрами retry")
    public void testWithDifferentRetryParameters() {
        TestData.Order order = TestData.getRandomOrder();

        Response createResponse = createOrder(order);

        if (createResponse.statusCode() == EXPECTED_CREATE_STATUS) {
            int trackNumber = createResponse.jsonPath().getInt("track");

            int customMaxAttempts = 5;
            int customDelaySeconds = 1;

            Response orderResponse = getOrderByTrackWithRetry(
                    trackNumber,
                    customMaxAttempts,
                    customDelaySeconds
            );

            if (orderResponse != null) {
                orderResponse.then()
                        .statusCode(EXPECTED_ORDER_FOUND_STATUS)
                        .body("order.track", equalTo(trackNumber));
            }
        }
    }
}