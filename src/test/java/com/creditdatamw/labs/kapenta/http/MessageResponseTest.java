package com.creditdatamw.labs.sparkpentaho.http;

import com.creditdatamw.labs.sparkpentaho.http.MessageResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the MessageResponse
 */
public class MessageResponseTest {

    @Test
    public void testToJson() {
        String expected = "{\"message\":\"Such an error\",\"error\":true}";

        assertEquals(expected, MessageResponse.errorJson("Such an error"));
    }
}
