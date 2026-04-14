package com.smartcampus.app.error;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    private static final Logger LOG = Logger.getLogger(SensorUnavailableExceptionMapper.class.getName());

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        LOG.log(Level.INFO, "Sensor unavailable: {0}", exception.getMessage());
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("error", "SENSOR_UNAVAILABLE");
        json.put("message", exception.getMessage());
        return Response.status(Response.Status.FORBIDDEN).entity(json).build();
    }
}
