package com.example;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.Config;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static junit.framework.TestCase.assertNotNull;

public class CourierCreationTest extends BaseTest {

    private static final int EXPECTED_CREATE_STATUS = 201;
    private static final int[] DUPLICATE_COURIER_EXPECTED_CODES = {409, 400};

    @Test
    @DisplayName("Успешное создание курьера")
    public void testCreateCourierSuccess() {
        TestData.Courier courier = TestData.getRandomCourier();

        createdCourierId = createCourier(courier);
        assertNotNull("Курьер должен быть создан и авторизован", createdCourierId);
    }

    @Test
    @DisplayName("Создание дубликата курьера")
    public void testCreateDuplicateCourier() {
        TestData.Courier courier = TestData.getRandomCourier();

        createdCourierId = createCourier(courier);
        assertNotNull("Первый курьер должен быть создан", createdCourierId);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(courier)
                .when()
                .post(Config.CREATE_COURIER);

        assertStatusCodeFlexible(response, DUPLICATE_COURIER_EXPECTED_CODES,
                "Создание дубликата курьера");
    }

    @Test
    @DisplayName("Создание курьера без логина")
    public void testCreateCourierWithoutLogin() {
        TestData.Courier courier = TestData.getRandomCourier();
        courier.setLogin(null);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(courier)
                .when()
                .post(Config.CREATE_COURIER);

        int[] expectedCodes = {400, 422};
        assertStatusCodeFlexible(response, expectedCodes, "Создание курьера без логина");
    }

    @Test
    @DisplayName("Создание курьера без пароля")
    public void testCreateCourierWithoutPassword() {
        TestData.Courier courier = TestData.getRandomCourier();
        courier.setPassword(null);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(courier)
                .when()
                .post(Config.CREATE_COURIER);

        int[] expectedCodes = {400, 422};
        assertStatusCodeFlexible(response, expectedCodes, "Создание курьера без пароля");
    }

    @Test
    @DisplayName("Создание курьера без имени")
    public void testCreateCourierWithoutFirstName() {
        TestData.Courier courier = TestData.getRandomCourier();
        courier.setFirstName(null);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(courier)
                .when()
                .post(Config.CREATE_COURIER);

        int[] expectedCodes = {201, 400};
        assertStatusCodeFlexible(response, expectedCodes, "Создание курьера без имени");

        if (response.statusCode() == 201) {
            createdCourierId = loginCourier(courier.getLogin(), courier.getPassword());
        }
    }

    @Test
    @DisplayName("Тест с логированием статуса")
    public void testWithStatusLogging() {
        TestData.Courier courier = TestData.getRandomCourier();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(courier)
                .when()
                .post(Config.CREATE_COURIER);

        assertStatusCodeWithLogging(response, EXPECTED_CREATE_STATUS,
                "Создание курьера с логированием");
    }
}