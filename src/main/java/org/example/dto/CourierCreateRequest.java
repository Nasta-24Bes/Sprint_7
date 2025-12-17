package org.example.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CourierCreateRequest {
    private final String login;
    private final String password;
    private final String firstName;

    @JsonCreator
    public CourierCreateRequest(
            @JsonProperty("login") String login,
            @JsonProperty("password") String password,
            @JsonProperty("firstName") String firstName) {
        this.login = login;
        this.password = password;
        this.firstName = firstName;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }
}