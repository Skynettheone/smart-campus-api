package com.smartcampus.app.api;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.smartcampus.app.Main;
import com.smartcampus.app.store.CampusData;

/**
 * GET /api/v1 — tells clients what this API is and where collections live.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    private static final String API_VERSION = "1.0.0";

    private final CampusData data;

    @Inject
    public DiscoveryResource(CampusData data) {
        this.data = data;
    }

    @GET
    public Map<String, Object> discover() {
        String base = Main.BASE_URL + "/api/v1";
        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms", base + "/rooms");
        links.put("sensors", base + "/sensors");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Smart Campus API");
        body.put("apiVersion", API_VERSION);
        body.put("adminContact", "smart-campus-support@university.example");
        body.put("_links", links);
        body.put("note", "In-memory demo store — room count is " + data.listRooms().size());
        return body;
    }
}
