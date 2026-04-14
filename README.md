# “Smart Campus” Sensor & Room Management API

## Client-Server Architectures - Coursework (2025/26)

<small>Tharuka Sanketh Karunanayaka | 20232782 | w2152940</small>

---

This project is a REST API for the Smart Campus scenario: it manages **rooms**, **sensors**, and **sensor readings**. It is built with **JAX-RS (Jersey)**, **Java 11**, and **Maven**, and runs on an embedded **Grizzly** server. Data is kept **in memory** (maps and lists) as required for the assignment: **no Spring** and **no database**.

## Capabilities

| Area | Behaviour |
|------|-----------|
| **Discovery** | `GET /api/v1` returns API metadata, administrative contact, and hypermedia-style links to primary collections. |
| **Rooms** | List, create (`201` + `Location`), retrieve by id, delete when no sensors remain on the room (`409` if sensors are still assigned). |
| **Sensors** | Register with validation that `roomId` exists (`422` if not); list all sensors with optional `?type=` filter. |
| **Readings** | Nested under `/api/v1/sensors/{sensorId}/readings` via a sub-resource locator; `POST` appends history and updates the parent sensor’s `currentValue`; `403` when the sensor is in `MAINTENANCE`. |
| **Errors & observability** | Structured JSON for domain errors; global handler avoids leaking stack traces on `500`; request/response logging via JAX-RS filters and `java.util.logging`. |

Base path: **`/api/v1`**.

## Build and run

### NetBeans

1. **File → Open Project…** and select the folder that contains `pom.xml`.
2. Allow Maven to resolve dependencies on first open.
3. Run **`com.smartcampus.app.Main`** (Run File or set as main class and Run Project).
4. Service listens on **`http://localhost:8080`**. Stop by pressing **Enter** in the application console or stopping the run configuration.

### Command line

```bash
cd smart-campus-api
mvn compile exec:java
```

## Example `curl` requests

Adjust host or port if your configuration differs.

**1. Discovery**

```bash
curl -s http://localhost:8080/api/v1
```

**2. Create a room (`201` + `Location`)**

```bash
curl -s -i -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LIB-301\",\"name\":\"Library quiet study\",\"capacity\":40}"
```

**3. Register a sensor (valid `roomId`)**

```bash
curl -s -i -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"LIB-301\"}"
```

**4. Invalid `roomId` (`422`, JSON body)**

```bash
curl -s -i -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-99\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"DOES-NOT-EXIST\"}"
```

**5. Append a reading (updates parent `currentValue`)**

```bash
curl -s -i -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":450.5,\"timestamp\":0}"
```

**6. Filter sensors by type**

```bash
curl -s "http://localhost:8080/api/v1/sensors?type=CO2"
```

**7. Delete room that still has sensors (`409`)**

```bash
curl -s -i -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

## Source layout

| Location | Role |
|----------|------|
| `com.smartcampus.app.Main` | Boots Grizzly and publishes the JAX-RS application. |
| `SmartCampusApplication` | `@ApplicationPath("/api/v1")`, Jackson, resources, exception mappers, logging filter. |
| `store.CampusData` | In-memory store; synchronized access for concurrent requests. |
| `api.*` | Resource classes and sub-resources. |
| `error.*` | Custom exceptions and `ExceptionMapper` implementations. |
| `filter.RequestResponseLoggingFilter` | Request method/URI and response status logging. |

---

<sub><small>University of Westminster - Client-Server Architectures (<strong>5COSC022W</strong>), coursework 2025/26.</small></sub>
