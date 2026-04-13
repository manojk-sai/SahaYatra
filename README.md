# Saha Yatra — Backend API

> **Collaborative travel route planning** — Spring Boot 3 · MongoDB · JWT · Phase 1–5 complete

A production-ready REST API for planning, voting on, and exporting collaborative travel itineraries. Built as a phased learning project demonstrating real-world MongoDB document modelling, Spring Security, AOP, async event publishing, and the Strategy design pattern.

---

## Live Demo

| Resource | URL |
|---|---|
| API Base | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.3.2 |
| Language | Java 21 |
| Database | MongoDB (Spring Data MongoDB) |
| Auth | Spring Security + JWT (jjwt 0.12.5) |
| API Docs | Springdoc OpenAPI 2.5.0 |
| AOP | Spring AOP (`@RequiresTripRole`) |
| Async | `@Async` + `@EnableAsync` thread pool |
| Testing | JUnit 5 + Mockito + Flapdoodle Embedded MongoDB |
| Build | Maven |
| Utilities | Lombok |

---

## MongoDB Document Schema Relationship summary

```
users   ──────────  notifications[]    One-to-Many  (embedded, max 100)
users   ──────────  UserProfile        One-to-One   (embedded object)
trips   ──────────  stops[]            One-to-Many  (embedded array)
stops   ──────────  votes[]            One-to-Many  (embedded inside each stop)
trips   ↔─────────  users              Many-to-Many (via members[] array with role metadata)
```

> **Why embed instead of reference?** Reads are the dominant operation. Embedding stops and votes inside `trips` means a single document fetch returns the full trip state — no joins needed. Notifications are embedded in `users` for the same reason: a user's inbox is always fetched in the context of that user only.

---

## Project Structure

```
src/main/java/com/routewise/travel/
│
├── aop/                    # Cross-cutting concerns
│   ├── RequiresTripRole.java      # Custom annotation
│   └── TripRoleAspect.java        # Enforces ORGANIZER > CONTRIBUTOR > VIEWER
│
├── controller/             # REST endpoints
│   ├── TripController.java        # /trips/**
│   ├── VotingController.java      # /trips/{id}/stops/{id}/vote|votes|status
│   ├── NotificationController.java# /notifications
│   └── ItineraryController.java   # /trips/{id}/itinerary
│
├── dto/
│   ├── request/                   # Validated input bodies
│   └── response/                  # Shaped output DTOs
│
├── entity/
│   ├── User.java                  # @Document — users collection
│   ├── Trip.java                  # @Document — trips collection
│   └── embedded/                  # Plain classes (no @Document)
│       ├── Stop.java
│       ├── Vote.java
│       ├── TripMembership.java
│       └── Notification.java
│
├── enums/                  # All enum types
├── event/                  # Spring ApplicationEvents (Phase 5)
├── exception/              # GlobalExceptionHandler (@RestControllerAdvice)
├── repository/             # MongoRepository interfaces
├── security/               # JWT filter + SecurityConfig
│
├── service/
│   ├── TripService.java           # Core trip business logic
│   ├── VotingService.java         # castVote, getTally, overrideStatus
│   ├── NotificationService.java   # push / mark-read via MongoTemplate
│   ├── NotificationListener.java  # @Async @EventListener handlers
│   └── ItineraryService.java      # Day-grouped itinerary builder
│
└── voting/                 # Strategy Pattern
    ├── VotingStrategy.java        # Interface
    ├── MajorityStrategy.java      # >50% approve → CONFIRMED
    ├── UnanimousStrategy.java     # Any REJECT → REJECTED
    ├── OrganizerStrategy.java     # Always PROPOSED (advisory only)
    └── VotingStrategyFactory.java # @Component factory
```

---

## API Reference

### Auth
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/auth/register` | — | Register user, returns JWT |
| `POST` | `/auth/login` | — | Login, returns JWT |

### Users
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/users/me` | JWT | Get current user profile |
| `PUT` | `/users/me` | JWT | Update profile |

### Trips
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/trips` | JWT | Create trip (caller becomes ORGANIZER) |
| `GET` | `/trips/my` | JWT | All trips for current user |
| `GET` | `/trips/public` | — | Publicly visible trips |
| `GET` | `/trips/{id}` | JWT · member | Get full trip |
| `PATCH` | `/trips/{id}` | JWT · ORGANIZER | Update trip details |
| `POST` | `/trips/{id}/advance` | JWT · ORGANIZER | Advance lifecycle status |
| `POST` | `/trips/{id}/lock` | JWT · ORGANIZER | Lock trip |

### Stops
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/trips/{id}/stops` | JWT · CONTRIBUTOR | Add stop → fires `StopProposedEvent` |
| `DELETE` | `/trips/{id}/stops/{stopId}` | JWT · CONTRIBUTOR | Remove stop |
| `PATCH` | `/trips/{id}/stops/reorder` | JWT · CONTRIBUTOR | Reorder stops |

### Members & Invites
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/trips/{id}/invite` | JWT · ORGANIZER | Generate invite token |
| `POST` | `/trips/join?token=` | JWT | Accept invite by token |
| `POST` | `/trips/{id}/leave` | JWT · member | Leave trip (soft delete) |
| `POST` | `/trips/{id}/transfer` | JWT · ORGANIZER | Transfer organizer role |

### Voting
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/trips/{id}/stops/{stopId}/vote` | JWT · member | Cast/replace vote |
| `GET` | `/trips/{id}/stops/{stopId}/votes` | JWT · member | Full vote tally |
| `PATCH` | `/trips/{id}/stops/{stopId}/status` | JWT · ORGANIZER | Manual status override |

### Itinerary
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/trips/{id}/itinerary` | JWT · member | Day-grouped itinerary with cost breakdown |

### Notifications
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/notifications` | JWT | All notifications (`?unread=true` to filter) |
| `PATCH` | `/notifications/{id}/read` | JWT | Mark one notification read |
| `PATCH` | `/notifications/read-all` | JWT | Mark all notifications read |

---

## Design Patterns

### Strategy Pattern — Voting
`VotingMode` on a `Trip` determines which strategy resolves stop votes:

| Mode | Behaviour |
|---|---|
| `MAJORITY` | Confirmed if >50% approve; rejected if >50% reject |
| `UNANIMOUS` | Any single REJECT immediately rejects the stop |
| `ORGANIZER` | Votes are advisory only — organizer overrides manually |

### AOP — Role Guard
`@RequiresTripRole(MemberRole.ORGANIZER)` on a service method automatically loads the trip, finds the caller's `TripMembership`, and enforces the role hierarchy (`VIEWER < CONTRIBUTOR < ORGANIZER`) before the method body executes.

### Event-Driven Notifications
Service methods publish `ApplicationEvent` subclasses. `NotificationListener` handles them `@Async` so the HTTP response is never blocked:

```
addStop()        → StopProposedEvent    → notify all members except proposer
castVote()       → VoteResolvedEvent    → notify all members when stop auto-resolves
acceptInvite()   → MemberJoinedEvent    → notify organizer
advanceStatus()  → TripStatusChangedEvent → notify all members
```

---

## Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- MongoDB 7+ (or Docker)

### 1 — Clone and configure
```bash
git clone https://github.com/your-username/saha-yatra-backend.git
cd saha-yatra-backend
```

### 2 — Start MongoDB
```bash
# Option A: Docker (recommended)
docker run -d --name mongo -p 27017:27017 mongo:7

# Option B: use docker-compose.yml in the repo
docker-compose up -d
```

### 3 — Set environment variables
```bash
export MONGODB_URI=mongodb://localhost:27017/routewise
export JWT_SECRET=your-secret-key-minimum-32-characters-long
```
Or copy `.env.example` to `.env` and fill in the values.

### 4 — Run
```bash
mvn spring-boot:run
```

The API is now live at `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Environment Variables

| Variable | Default | Required | Description |
|---|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/routewise` | No | MongoDB connection string |
| `JWT_SECRET` | *(insecure default)* | **Yes in prod** | HS256 signing key, min 32 chars |
| `SERVER_PORT` | `8080` | No | HTTP port |

---

## Main Application Class

```java
@SpringBootApplication
@EnableMongoAuditing     // required for @CreatedDate / @LastModifiedDate
@EnableAsync             // required for @Async NotificationListener
@EnableScheduling        // required for @Scheduled weather refresh
public class TravelApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelApplication.class, args);
    }
}
```
---

## Phase History

| Phase | Feature | Key patterns |
|---|---|---|
| 1 | Auth, JWT, User + embedded profile | `@Document`, embedded One-to-One |
| 2 | Trips, stops, membership, invite flow | Embedded One-to-Many, Many-to-Many via `members[]` |
| 3 | Voting system | Strategy pattern, `MongoTemplate $push`, atomic status update |
| 4 | Weather snapshots | `@Async`, `@Scheduled`, `$set` array filter |
| 5 | Notifications, itinerary export | Spring Events, `@EventListener`, `$push`, day-grouped aggregation |
