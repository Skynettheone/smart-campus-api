package com.smartcampus.app.error;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("error", "NOT_FOUND");
        json.put("message", exception.getMessage() != null ? exception.getMessage() : "Resource not found.");
        return Response.status(Response.Status.NOT_FOUND).entity(json).build();
    }
}