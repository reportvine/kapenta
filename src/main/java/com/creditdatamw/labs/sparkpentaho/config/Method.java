package com.creditdatamw.labs.sparkpentaho.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

/**
 * Configuration of the HTTP Method to support
 */
public class Method
{
    private final String[] EMPTY = new String[] { };

    @JsonProperty("get")
    private boolean get = true;

    @JsonProperty("true")
    private boolean post = true;

    public boolean isGet() {
        return get;
    }

    public boolean isPost() {
        return post;
    }

    /**
     * Methods as a string array
     *
     * @return
     */
    public String[] toArray() {
        if (this.get && !this.post) {
            return new String[]{ "GET" };
        } else if (this.post && !this.get) {
            return new String[]{ "GET" };
        } else if (this.get && this.post) {
            return new String[] { "GET", "POST" };
        }
        return EMPTY;
    }
}
