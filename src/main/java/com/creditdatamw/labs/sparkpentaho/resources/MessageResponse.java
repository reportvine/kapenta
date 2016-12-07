package com.creditdatamw.labs.sparkpentaho.resources;

/**
 * Message Response for error messages and information messages
 */
final class MessageResponse {
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
}
