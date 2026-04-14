package com.smartcampus.app.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.smartcampus.app.model.Sensor;
import com.smartcampus.app.store.CampusData;

@Path("sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final CampusData data;

    @Inject
    public SensorResource(CampusData data) {
        this.data = data;
    }

    /**
     * Sub-resource locator — coursework asks for a separate class for readings.
     */
    @Path("{sensorId}/readings")
    public SensorReadingResource readings(@javax.ws.rs.PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(data, sensorId);
    }

    @GET
    public List<Sensor> list(@QueryParam("type") String type) {
        return data.listSensors(type);
    }

    @POST
    public Response register(Sensor sensor) {
        try {
            data.addSensor(sensor);
        } catch (IllegalStateException ex) {
            return conflict(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return badRequest(ex.getMessage());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Sensor registered");
        body.put("id", sensor.getId());
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    private static Response badRequest(String message) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("error", "BAD_REQUEST");
        json.put("message", message);
        return Response.status(Response.Status.BAD_REQUEST).entity(json).build();
    }

    private static Response conflict(String message) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("error", "CONFLICT");
        json.put("message", message);
        return Response.status(Response.Status.CONFLICT).entity(json).build();
    }
}
