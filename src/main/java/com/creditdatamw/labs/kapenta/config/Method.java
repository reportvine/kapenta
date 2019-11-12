package com.creditdatamw.labs.kapenta.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration of the HTTP Method to support
 */
public class Method
{
    /**
     * All methods GET and POST
     */
    public static final Method ALL = new Method(true, true);
    
    /**
     * Only the GET method
     */
    public static final Method GET = new Method(true, false);
    
    /**
     * Only the POST method
     */
    public static final Method POST = new Method(false, true);

    private final String[] EMPTY = new String[] { };

    @JsonProperty("get")
    private boolean get = true;

    @JsonProperty("post")
    private boolean post = true;

    @JsonCreator
    private Method(@JsonProperty("get") boolean get, @JsonProperty("post") boolean post) {
        this.get = get;
        this.post = post;
    }

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
            return new String[]{ "POST" };
        } else if (this.get && this.post) {
            return new String[] { "GET", "POST" };
        }
        return EMPTY;
    }
}
