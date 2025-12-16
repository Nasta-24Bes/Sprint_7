package org.example.api;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.example.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OrderClient extends BaseClient {

    public static Response createOrder(org.example.dto.OrderCreateRequest order) {
        return post(Config.CREATE_ORDER, order);
    }

    public static Response getOrderByTrack(int trackNumber) {
        return getBaseSpec()
                .queryParam("t", trackNumber)
                .when()
                .get(Config.GET_ORDER_BY_TRACK);
    }

    public static Response getOrderByTrackWithRetry(int trackNumber, int maxAttempts, int delaySeconds) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Response response = getOrderByTrack(trackNumber);

            if (response.statusCode() == 200) {
                return response;
            }

            if (attempt < maxAttempts) {
                try {
                    TimeUnit.SECONDS.sleep(delaySeconds);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return null;
    }

    public static Response cancelOrder(int trackNumber) {
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("track", trackNumber);
        return put(Config.CANCEL_ORDER, requestBody);
    }

    public static Response getOrdersList(Integer limit, String nearestStation) {
        RequestSpecification spec = getBaseSpec();

        if (limit != null) {
            spec.queryParam("limit", limit);
        }
        if (nearestStation != null) {
            spec.queryParam("nearestStation", nearestStation);
        }

        return spec.when().get(Config.GET_ORDERS);
    }

    public static Integer getTrackNumberFromResponse(Response createResponse) {
        if (createResponse.statusCode() == 201) {
            return createResponse.as(org.example.dto.OrderCreateResponse.class).getTrack();
        }
        return null;
    }

    public static Response acceptOrder(Integer orderId, Integer courierId) {
        Map<String, Integer> requestBody = new HashMap<>();
        if (courierId != null) {
            requestBody.put("courierId", courierId);
        }

        return put("/api/v1/orders/accept/" + (orderId != null ? orderId : ""), requestBody);
    }
}