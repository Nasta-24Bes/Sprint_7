package org.example.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderCreateResponse {
    private final Integer track;

    @JsonCreator
    public OrderCreateResponse(@JsonProperty("track") Integer track) {
        this.track = track;
    }

    public Integer getTrack() {
        return track;
    }
}