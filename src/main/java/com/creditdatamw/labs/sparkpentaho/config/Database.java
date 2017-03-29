package com.creditdatamw.labs.sparkpentaho.config;

/**
 * Database configuration for Pentaho reports
 */
public class Database {

    private String user;

    private String password;

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
