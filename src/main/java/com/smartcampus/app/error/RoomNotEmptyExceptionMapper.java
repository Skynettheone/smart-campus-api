package com.smartcampus.app.error;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    private static final Logger LOG = Logger.getLogger(RoomNotEmptyExceptionMapper.class.getName());

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        LOG.log(Level.INFO, "Room not empty: {0}", exception.getMessage());
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("error", "ROOM_NOT_EMPTY");
        json.put("message", exception.getMessage());
        return Response.status(Response.Status.CONFLICT).entity(json).build();
    }
}
