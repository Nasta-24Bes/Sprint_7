package org.example.api;

import io.restassured.response.Response;
import org.example.Config;
import org.example.dto.CourierCreateRequest;
import org.example.dto.CourierLoginRequest;

public class CourierClient extends BaseClient {

    public static Response createCourier(CourierCreateRequest courier) {
        return post(Config.CREATE_COURIER, courier);
    }

    public static Response loginCourier(CourierLoginRequest loginData) {
        return post(Config.LOGIN_COURIER, loginData);
    }

    public static Response deleteCourier(String id) {
        return delete(Config.DELETE_COURIER + id);
    }
}