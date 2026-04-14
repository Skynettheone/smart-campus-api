package com.smartcampus.app.error;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Last-resort handler so clients never see a Java stack trace in the HTTP body.
 */
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }
        LOG.log(Level.SEVERE, "Unexpected error", exception);
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("error", "INTERNAL_ERROR");
        json.put("message", "Something went wrong on the server. Please try again later.");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json).build();
    }
}
