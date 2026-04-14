package com.smartcampus.app.error;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    private static final Logger LOG = Logger.getLogger(LinkedResourceNotFoundExceptionMapper.class.getName());

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        LOG.log(Level.INFO, "Linked resource missing: {0}", exception.getMessage());
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("error", "LINKED_RESOURCE_NOT_FOUND");
        json.put("message", exception.getMessage());
        return Response.status(422).entity(json).build();
    }
}
