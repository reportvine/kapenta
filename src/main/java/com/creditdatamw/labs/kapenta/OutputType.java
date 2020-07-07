package com.creditdatamw.labs.kapenta;

/**
 * Supported output types
 */
public enum OutputType {
    NONE("none"),
    PDF("application/pdf"),
    HTML("text/html"),
    TXT("text/plain");

    final String contentType;

    OutputType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContentTypeUtf8() {
        return getContentType("utf-8");
    }

    public String getContentType(String charset) {
        return String.format("%s;charset=%s", contentType, charset);
    }
}
