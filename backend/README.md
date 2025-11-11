# CRM Backend

Spring Boot backend for internal CRM system.

## Requirements

- Java 17+
- Maven (or use included `mvnw`)

## How to Run

### Development Mode (Default - H2 In-Memory DB)

```bash
./mvnw spring-boot:run
```

The application will start on port **8090** with the `dev` profile active, using an in-memory H2 database.

### Production Mode (PostgreSQL - Placeholders)

```bash
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

**Note:** Production mode uses PostgreSQL placeholders. Update `application-prod.yml` with your actual database credentials before connecting to a real database.

## Verify

### Health Check

```bash
curl http://localhost:8090/api/health
```

Expected response:
```json
{
  "status": "UP",
  "app": "crm-backend",
  "time": "2024-01-15T10:30:00Z"
}
```

### Swagger UI

Open your browser and navigate to:

```
http://localhost:8090/swagger-ui.html
```

## Profiles

- **dev**: H2 in-memory database, auto schema update, JPA open-in-view disabled
- **prod**: PostgreSQL (placeholders), schema validation, JPA open-in-view disabled

## Port

Default server port: **8090**

