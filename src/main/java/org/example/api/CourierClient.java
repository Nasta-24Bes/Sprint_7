package org.example.api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.example.Config;
import org.example.dto.CourierCreateRequest;
import org.example.dto.CourierLoginRequest;

public class CourierClient extends BaseClient {

    @Step("Создание курьера")
    public static Response createCourier(CourierCreateRequest courier) {
        return post(Config.CREATE_COURIER, courier);
    }

    @Step("Авторизация курьера")
    public static Response loginCourier(CourierLoginRequest loginData) {
        return post(Config.LOGIN_COURIER, loginData);
    }

    @Step("Удаление курьера")
    public static Response deleteCourier(String id) {
        return delete(Config.DELETE_COURIER + id);
    }
}
