package com.smartcampus.app.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.smartcampus.app.error.LinkedResourceNotFoundException;
import com.smartcampus.app.error.RoomNotEmptyException;
import com.smartcampus.app.error.SensorUnavailableException;
import com.smartcampus.app.model.Room;
import com.smartcampus.app.model.Sensor;
import com.smartcampus.app.model.SensorReading;

/**
 * Simple in-memory store. Methods are {@code synchronized} so one request * finishes before another changes the maps (good enough for coursework).
 */
public class CampusData {

    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<String, Sensor> sensors = new HashMap<>();
    private final Map<String, List<SensorReading>> readingsBySensor = new HashMap<>();

    public synchronized List<Room> listRooms() {
        return new ArrayList<>(rooms.values());
    }

    public synchronized Room getRoom(String id) {
        return rooms.get(id);
    }

    public synchronized void addRoom(Room room) {
        if (room.getId() == null || room.getId().isBlank()) {
            throw new IllegalArgumentException("Room id is required");
        }
        if (rooms.containsKey(room.getId())) {
            throw new IllegalStateException("Room already exists: " + room.getId());
        }
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }
        rooms.put(room.getId(), room);
    }

    public synchronized void deleteRoom(String id) {
        Room room = rooms.get(id);
        if (room == null) {
            return;
        }
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
 "Cannot delete room " + id + " while it still has sensors assigned.");
        }
        rooms.remove(id);
    }

    public synchronized List<Sensor> listSensors(String typeFilter) {
        return sensors.values().stream()
                .filter(s -> typeFilter == null
                        || typeFilter.isBlank()
                        || typeFilter.equalsIgnoreCase(s.getType()))
                .collect(Collectors.toList());
    }

    public synchronized Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public synchronized void addSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            throw new IllegalArgumentException("Sensor id is required");
        }
        if (sensors.containsKey(sensor.getId())) {
            throw new IllegalStateException("Sensor already exists: " + sensor.getId());
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            throw new IllegalArgumentException("roomId is required");
        }
        Room room = rooms.get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "No room with id \"" + sensor.getRoomId() + "\".");
        }
        sensors.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());
        readingsBySensor.putIfAbsent(sensor.getId(), new ArrayList<>());
    }

    public synchronized List<SensorReading> listReadings(String sensorId) {
        List<SensorReading> list = readingsBySensor.get(sensorId);
        if (list == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(list);
    }

    public synchronized void addReading(String sensorId, SensorReading reading) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            throw new IllegalStateException("Sensor should exist before adding a reading: " + sensorId);
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor " + sensorId + " is in MAINTENANCE and cannot accept new readings.");
        }
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() <= 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }
        readingsBySensor.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
        sensor.setCurrentValue(reading.getValue());
    }
}
