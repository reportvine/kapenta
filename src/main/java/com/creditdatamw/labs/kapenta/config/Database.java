package com.creditdatamw.labs.kapenta.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Database configuration for Pentaho generator
 */
public class Database {

    @JsonProperty
    private String user;

    @JsonProperty
    private String password;

    @JsonProperty
    private String uri;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
