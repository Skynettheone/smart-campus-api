package com.smartcampus.app.api;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.smartcampus.app.Main;
import com.smartcampus.app.error.RoomNotEmptyException;
import com.smartcampus.app.model.Room;
import com.smartcampus.app.store.CampusData;

/**
 * Coursework name: SensorRoom resource — manages /rooms.
 */
@Path("rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    private final CampusData data;

    @Inject
    public SensorRoomResource(CampusData data) {
        this.data = data;
    }

    @GET
    public List<Room> listAll() {
        return data.listRooms();
    }

    @POST
    public Response create(Room room) {
        try {
            data.addRoom(room);
        } catch (IllegalStateException ex) {
            return conflict(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return badRequest(ex.getMessage());
        }
        URI location = URI.create(Main.BASE_URL + "/api/v1/rooms/" + room.getId());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Room created");
        body.put("id", room.getId());
        return Response.created(location).entity(body).build();
    }

    @GET
    @Path("{id}")
    public Response getOne(@PathParam("id") String id) {
        Room room = data.getRoom(id);
        if (room == null) {
            return notFound("No room with id \"" + id + "\".");
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") String id) {
        Room room = data.getRoom(id);
        if (room == null) {
            return notFound("No room with id \"" + id + "\".");
        }
        try {
            data.deleteRoom(id);
        } catch (RoomNotEmptyException ex) {
            throw ex;
        }
        return Response.noContent().build();
    }

    private static Response notFound(String message) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("error", "NOT_FOUND");
        json.put("message", message);
        return Response.status(Response.Status.NOT_FOUND).entity(json).build();
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
