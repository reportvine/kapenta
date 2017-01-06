package com.creditdatamw.labs.sparkpentaho.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BasicAuth {

    @JsonProperty(required = false)
    private User user;

    @JsonProperty(required = false)
    private List<User> users;

    public static class User {
        @JsonProperty
        private String username;

        @JsonProperty
        private String password;
    }
}
