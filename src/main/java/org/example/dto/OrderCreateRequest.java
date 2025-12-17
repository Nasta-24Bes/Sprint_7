package org.example.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@SuppressWarnings("unused")
public class OrderCreateRequest {
    private final String firstName;
    private final String lastName;
    private final String address;
    private final String metroStation;
    private final String phone;
    private final Integer rentTime;
    private final String deliveryDate;
    private final String comment;
    private final List<String> color;

    @JsonCreator
    public OrderCreateRequest(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("address") String address,
            @JsonProperty("metroStation") String metroStation,
            @JsonProperty("phone") String phone,
            @JsonProperty("rentTime") Integer rentTime,
            @JsonProperty("deliveryDate") String deliveryDate,
            @JsonProperty("comment") String comment,
            @JsonProperty("color") List<String> color) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.metroStation = metroStation;
        this.phone = phone;
        this.rentTime = rentTime;
        this.deliveryDate = deliveryDate;
        this.comment = comment;
        this.color = color;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAddress() { return address; }
    public String getMetroStation() { return metroStation; }
    public String getPhone() { return phone; }
    public Integer getRentTime() { return rentTime; }
    public String getDeliveryDate() { return deliveryDate; }
    public String getComment() { return comment; }
    public List<String> getColor() { return color; }
}