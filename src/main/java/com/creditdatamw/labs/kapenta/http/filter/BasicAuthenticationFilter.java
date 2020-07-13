package com.creditdatamw.labs.kapenta.http.filter;

import com.creditdatamw.labs.kapenta.config.BasicAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Filter;
import spark.Request;
import spark.Response;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.creditdatamw.labs.kapenta.http.MessageResponse.errorJson;
import static spark.Spark.halt;

/**
 * Filter for Basic Authentication using a {@link BasicAuth.User}
 */
public class BasicAuthenticationFilter implements Filter {
    private final static Logger LOGGER = LoggerFactory.getLogger(BasicAuthenticationFilter.class);
    private final String HEADER_NAME = "Authorization";
    private final String BASIC_PREFIX = "Basic ";

    private final List<BasicAuth.User> users;

    public BasicAuthenticationFilter(BasicAuth.User user) {
        Objects.requireNonNull(user, "User cannot be null");
        this.users = Collections.singletonList(user);
    }

    public BasicAuthenticationFilter(List<BasicAuth.User> users) {
        Objects.requireNonNull(users, "User List cannot be null");
        this.users = Collections.unmodifiableList(users);
    }

    @Override
    public void handle(Request request, Response response) throws Exception {
        if (! request.headers().contains(HEADER_NAME)) {
            LOGGER.error("Failed to find Authorization header in request.");
            halt(401, errorJson("Not Authorized to access this report"));
            return;
        }

        String val = request.headers(HEADER_NAME);

        if (val.length() < BASIC_PREFIX.length()) {
            LOGGER.error("Failed to find Authorization value in request.");
            halt(401, errorJson("Not Authorized to access this report"));
            return;
        }

        String base64Encoded = val.substring(BASIC_PREFIX.length());

        if (base64Encoded.isEmpty()) {
            LOGGER.error("Failed to find Authorization value in request.");
            halt(401, errorJson("Not Authorized to access this report"));
            return;
        }

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decoded = decoder.decode(base64Encoded);

        String[] usernamePassword = new String(decoded, Charset.forName("UTF-8")).split(":");

        String username = usernamePassword[0],
            password = usernamePassword[1];

        if (! users.contains(new BasicAuth.User(username, password))) {
            LOGGER.error("Failed to find User in list of authorized users value in request.");
            halt(401, errorJson("Not Authorized to access this report"));
            return;
        }
    }
}
