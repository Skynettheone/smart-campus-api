package com.smartcampus.app.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.smartcampus.app.error.SensorUnavailableException;
import com.smartcampus.app.model.SensorReading;
import com.smartcampus.app.store.CampusData;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final CampusData data;
    private final String sensorId;

    public SensorReadingResource(CampusData data, String sensorId) {
        this.data = data;
        this.sensorId = sensorId;
    }

    @GET
    public List<SensorReading> history() {
        if (data.getSensor(sensorId) == null) {
            throw new NotFoundException("No sensor with id \"" + sensorId + "\".");
        }
        return data.listReadings(sensorId);
    }

    @POST
    public Response add(SensorReading reading) {
        if (reading == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "BAD_REQUEST");
            err.put("message", "Send JSON in the body (Content-Type: application/json).");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        if (data.getSensor(sensorId) == null) {
            throw new NotFoundException("No sensor with id \"" + sensorId + "\".");
        }
        try {
            data.addReading(sensorId, reading);
        } catch (SensorUnavailableException ex) {
            throw ex;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Reading stored");
        body.put("readingId", reading.getId());
        return Response.status(Response.Status.CREATED).entity(body).build();
    }
}
