package com.creditdatamw.labs.kapenta.http;

import static com.creditdatamw.labs.kapenta.Server.OBJECT_MAPPER;

/**
 * Message Response for error messages and information messages
 */
public final class MessageResponse {

    public final String message;
    public final boolean error;

    public MessageResponse(String message, boolean isError) {
        this.message = message;
        this.error = isError;
    }

    public String getMessage() {
        return message;
    }

    public boolean isError() {
        return error;
    }

    public static String errorJson(String message) {
        try {
            return OBJECT_MAPPER.writeValueAsString(new MessageResponse(message, true));
        } catch(Exception e) {
        }
        return "{\"message\":\"" + message + ",\"error\":true}";
    }
}
