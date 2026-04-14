package com.smartcampus.app;

import java.net.URI;
import java.util.Scanner;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

/**
 * Run this class from NetBeans: right-click → Run File (or Run Project).
 * Server base URL is http://localhost:8080 — API lives under /api/v1.
 */
public final class Main {

    public static final String BASE_URL = "http://localhost:8080";

    public static void main(String[] args) throws Exception {
        URI uri = URI.create(BASE_URL + "/");
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, new SmartCampusApplication());

        System.out.println("Smart Campus API is running.");
        System.out.println("Try: " + BASE_URL + "/api/v1");
        System.out.println("Press Enter in this console to stop the server...");
        try (Scanner scanner = new Scanner(System.in, java.nio.charset.StandardCharsets.UTF_8.name())) {
            scanner.nextLine();
        }
        server.shutdownNow();
    }

    private Main() {
    }
}
