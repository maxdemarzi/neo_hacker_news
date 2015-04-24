package com.maxdemarzi;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Exception extends WebApplicationException {

    public Exception(int code, String error)  {
        super(new Throwable(error), Response.status(code)
                .entity("{\"error\":\"" + error + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build());

    }

    public static Exception invalidInput = new Exception(400, "Invalid Input");
    public static Exception missingIdParameter = new Exception(400, "Missing id Parameter.");
    public static Exception missingURLParameter = new Exception(400, "Missing url Parameter.");
    public static Exception invalidIdParameter = new Exception(400, "Invalid id Parameter.");
    public static Exception invalidUsernameParameter = new Exception(400, "Invalid username Parameter.");
    public static Exception userNotFound = new Exception(404, "User not found.");
    public static Exception storyNotFound = new Exception(404, "Story not found.");
}
