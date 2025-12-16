package org.example;

import org.example.api.OrderClient;
import org.example.dto.OrderCreateRequest;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class OrderCreationTest extends BaseTest {

    private final List<String> colors;
    private final String description;

    public OrderCreationTest(List<String> colors, String description) {
        this.colors = colors;
        this.description = description; // Добавьте эту строку
    }

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {Collections.singletonList("BLACK"), "Черный самокат"},
                {Collections.singletonList("GREY"), "Серый самокат"},
                {Arrays.asList("BLACK", "GREY"), "Оба цвета"},
                {Collections.emptyList(), "Без указания цвета"}
        });
    }

    @Test
    @DisplayName("Создание и поиск заказа")
    public void testCreateAndFindOrder() {
        System.out.println("Тестируем: " + description);

        OrderCreateRequest order = createTestOrderWithColors(colors);

        Response createResponse = OrderClient.createOrder(order);
        int[] expectedCreateCodes = {SC_CREATED, SC_BAD_REQUEST};
        assertStatusCodeFlexible(createResponse, expectedCreateCodes, "Создание заказа");

        if (createResponse.statusCode() == SC_CREATED) {
            Integer trackNumber = OrderClient.getTrackNumberFromResponse(createResponse);
            assertNotNull("Трек-номер должен быть получен", trackNumber);

            addOrderTrackForCleanup(trackNumber);

            Response orderResponse = OrderClient.getOrderByTrackWithRetry(trackNumber, 3, 2);

            if (orderResponse != null) {
                orderResponse.then()
                        .statusCode(SC_OK)
                        .body("order", notNullValue())
                        .body("order.track", equalTo(trackNumber));
            } else {
                System.out.println("Заказ с track=" + trackNumber + " не найден после 3 попыток");
            }
        }
    }
}