# "Smart Campus" Sensor & Room Management API

## Client-Server Architectures - Coursework (2025/26)

<small>Tharuka Sanketh Karunanayaka | 20232782 | w2152940</small>

---

This project is a REST API for the Smart Campus scenario: it manages **rooms**, **sensors**, and **sensor readings**. It is built with **JAX-RS (Jersey)**, **Java 11**, and **Maven**, and runs on **Apache Tomcat** as a `.war` deployment. Data is kept **in memory** (maps and lists) as required for the assignment: **no Spring** and **no database**.

## Capabilities

| Area | Behaviour |
|------|-----------|
| **Discovery** | `GET /api/v1` returns API metadata, administrative contact, and hypermedia-style links to primary collections. |
| **Rooms** | List, create (`201` + `Location`), retrieve by id, delete when no sensors remain on the room (`409` if sensors are still assigned). |
| **Sensors** | Register with validation that `roomId` exists (`422` if not); list all sensors with optional `?type=` filter. |
| **Readings** | Nested under `/api/v1/sensors/{sensorId}/readings` via a sub-resource locator; `POST` appends history and updates the parent sensor's `currentValue`; `403` when the sensor is in `MAINTENANCE`. |
| **Errors & observability** | Structured JSON for domain errors; global handler avoids leaking stack traces on `500`; request/response logging via JAX-RS filters and `java.util.logging`. |

Base path: **`/api/v1`**.

## Build and run

### Prerequisites

- **Java 11** or higher
- **Apache Maven** 3.6+
- **Apache Tomcat 9.x** (tested with Tomcat 9.0.117)
- **NetBeans IDE** (tested with Apache NetBeans)

### NetBeans (Apache Tomcat)

1. **File → Open Project…** and select the folder that contains `pom.xml`.
2. Allow Maven to resolve dependencies on first open.
3. Ensure **Apache Tomcat** is configured under **Tools → Servers** (Tomcat 9.x recommended).
4. Right-click the project → **Properties → Run** and set the server to **Apache Tomcat**.
5. Right-click the project → **Run** (or press F6). NetBeans will build the `.war` and deploy it to Tomcat automatically.
6. The browser will open and automatically redirect to the API discovery endpoint.
7. If the redirect does not work, navigate manually to: **`http://localhost:8080/smart-campus-api/api/v1`**

### Command line

```bash
cd smart-campus-api
mvn clean package
```

Then copy `target/smart-campus-api.war` into your Tomcat `webapps/` directory and start Tomcat. The API will be available at: `http://localhost:8080/smart-campus-api/api/v1`

## Example `curl` requests

Adjust host, port, or context path if your configuration differs.

**1. Discovery**

```bash
curl -s http://localhost:8080/smart-campus-api/api/v1
```

**2. Create a room (`201` + `Location`)**

```bash
curl -s -i -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LIB-301\",\"name\":\"Library quiet study\",\"capacity\":40}"
```

**3. Register a sensor (valid `roomId`)**

```bash
curl -s -i -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"LIB-301\"}"
```

**4. Invalid `roomId` (`422`, JSON body)**

```bash
curl -s -i -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-99\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"DOES-NOT-EXIST\"}"
```

**5. Append a reading (updates parent `currentValue`)**

```bash
curl -s -i -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":450.5,\"timestamp\":0}"
```

**6. Filter sensors by type**

```bash
curl -s "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2"
```

**7. Delete room that still has sensors (`409`)**

```bash
curl -s -i -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

## Source layout

| Location | Role |
|----------|------|
| `SmartCampusApplication` | `@ApplicationPath("/api/v1")`, Jackson, resources, exception mappers, logging filter. |
| `store.CampusData` | In-memory store; synchronized access for concurrent requests. |
| `api.*` | Resource classes and sub-resources. |
| `error.*` | Custom exceptions and `ExceptionMapper` implementations. |
| `filter.RequestResponseLoggingFilter` | Request method/URI and response status logging. |
| `webapp/WEB-INF/web.xml` | Servlet configuration for Tomcat deployment. |
| `webapp/index.html` | Auto-redirect to the API discovery endpoint. |

## Design Decisions & Coursework Questions

### Part 1.1  -  JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new instance** of a resource class for every incoming request (per-request lifecycle). This means instance fields are not shared between requests. Since our `CampusData` store must persist across all requests, we inject a single shared instance via HK2 dependency injection and protect it with `synchronized` methods to prevent race conditions during concurrent access.

### Part 1.2  -  Hypermedia and HATEOAS

Hypermedia (HATEOAS) embeds navigational links directly inside API responses, allowing clients to discover available actions dynamically rather than hardcoding URLs. This decouples clients from server-side URI structures  -  if a path changes, the client simply follows the updated links. It also makes the API self-documenting, reducing reliance on external documentation.

### Part 2.1  -  Returning IDs vs Full Objects

Returning only IDs reduces bandwidth but forces the client to make **N additional GET requests** to fetch each room's details, increasing latency and server load. Returning full objects uses more bandwidth per response but eliminates extra round-trips, giving the client everything it needs in a single call. For a small dataset like campus rooms, returning full objects is the practical choice.

### Part 2.2  -  DELETE Idempotency

Yes, DELETE is **idempotent** in our implementation. The first `DELETE /rooms/LIB-301` removes the room and returns `204 No Content`. Subsequent identical DELETE requests find no room with that ID and return `404 Not Found`. The server state does not change after the first call  -  the room remains deleted regardless of how many times the request is repeated, which satisfies the definition of idempotency.

### Part 3.1  -  @Consumes and Content-Type Mismatch

If a client sends `text/plain` or `application/xml` to a method annotated with `@Consumes(MediaType.APPLICATION_JSON)`, the JAX-RS runtime automatically rejects the request with **HTTP 415 Unsupported Media Type** before the method is even invoked. The runtime matches the request's `Content-Type` header against the declared `@Consumes` media type, and because there is no match, no `MessageBodyReader` can be selected to deserialise the body.

### Part 3.2  -  @QueryParam vs Path-Based Filtering

Query parameters (`?type=CO2`) are superior for filtering because they are **optional by nature**  -  omitting the parameter returns all sensors, while including it narrows the results. A path-based approach (`/sensors/type/CO2`) treats the filter as a mandatory path segment, creating a rigid URL hierarchy. Query parameters also compose naturally: you can add `?type=CO2&status=ACTIVE` without restructuring the URI, which is impossible with path segments alone.

### Part 4.1  -  Sub-Resource Locator Pattern Benefits

The sub-resource locator pattern delegates nested paths to dedicated classes, following the **Single Responsibility Principle**. `SensorResource` handles sensor-level operations while `SensorReadingResource` handles reading-specific logic. This avoids a single monolithic controller with dozens of methods, improves readability, and allows each sub-resource to be developed and tested independently. It also naturally mirrors the domain hierarchy (sensors contain readings).

### Part 5.2 - HTTP 422 vs 404 for Missing References

HTTP 404 means the **requested URL does not exist**. But when a client POSTs valid JSON to a valid endpoint (`/api/v1/sensors`) and the problem is a `roomId` value inside the payload that references a non-existent room, the URL itself is fine. HTTP 422 (Unprocessable Entity) is more accurate here because the request syntax is correct, but the server cannot process it due to a semantic error in the data. Using 404 would mislead the client into thinking the endpoint is missing.

### Part 5.4 - Risks of Exposing Stack Traces

Exposing Java stack traces reveals **internal architecture details** to potential attackers: fully qualified class names, package structure, framework versions, database drivers, file paths, and line numbers. An attacker could use this information to identify known vulnerabilities in specific library versions, map the internal structure of the application, or craft targeted injection attacks. The `GlobalExceptionMapper` prevents this by returning a generic error message while logging the actual exception server-side.

### Part 5.5  -  JAX-RS Filters vs Manual Logging

JAX-RS filters implement logging as a **cross-cutting concern** that is automatically applied to every request and response without modifying any resource method. Manually inserting `Logger.info()` inside every method violates the DRY principle, creates code duplication, and risks inconsistency if a developer forgets to add logging to a new endpoint. Filters are also independently testable, can be enabled or disabled via configuration, and follow the Separation of Concerns principle taught in enterprise architecture.

---

<sub><small>University of Westminster - Client-Server Architectures (<strong>5COSC022W</strong>), coursework 2025/26.</small></sub>
