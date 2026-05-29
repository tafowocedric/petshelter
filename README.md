# Pet Shelter Management System

A multi-module Java web application for managing an animal shelter — animals, clients, and adoptions. Built without any web framework (no Spring) using plain JDBC, the JDK's built-in HTTP server, and a hand-written HTML rendering layer.

## Tech stack

- **Java 11**
- **Maven** (multi-module project)
- **PostgreSQL 16**
- **JDBC** (no ORM)
- **`com.sun.net.httpserver`** (built into the JDK — no web framework)
- **BCrypt** (password hashing via `at.favre.lib:bcrypt`)

## Module structure

```
petshelter/
├── db/         — JDBC connection, schema init, seed loader
├── core/       — entities, enums, exceptions
├── repository/ — DAO layer (JDBC queries)
├── service/    — business logic, auth, BCrypt
├── web/        — HTTP routing, controllers, HTML rendering
└── app/        — Main entry point
```

Each module has a single responsibility and only depends on the layers below it.

## Prerequisites

- JDK 11+
- Maven 3.8+

## Setup

```bash
# 1. Clone and enter the project
git clone https://github.com/<you>/petshelter.git
cd petshelter

# 2. Build all modules
mvn clean install

# 4. Run the application
mvn exec:java -pl app
```

The schema is created automatically on first startup, and seed data is inserted if the database is empty. The app listens on http://localhost:8080.

## Default credentials

| Role   | Username   | Password    |
| ------ | ---------- |-------------|
| Admin  | `admin`    | `admin1234` |
| Client | `john_doe` | `admin1234` |

## Features

### Anonymous
- Home page
- Sign in
- Register a new client account

### Client
- Browse available animals (filter by species)
- View animal details
- Request adoption with notes
- View own adoption history
- Cancel pending or approved adoptions

### Admin
- Dashboard with live stats
- Full animal CRUD (add, edit, delete, status managed automatically)
- User management (delete client accounts; admins are protected)
- Adoption approval workflow (approve / reject pending, complete approved)

## Status transitions

```
Animal:    AVAILABLE ──► PENDING ──► ADOPTED
                    ◄─────────────────┘ (if approved adoption is rejected/cancelled)

Adoption:  PENDING ──approve──► APPROVED ──complete──► COMPLETED
                └──reject────► REJECTED
                └──cancel────► REJECTED
```

The next app startup will recreate the schema and reseed the data.

## Demonstrated Java techniques

| # | Technique | Where |
| - | --------- | ----- |
| 1 | Encapsulation | All model classes, private fields with validation in setters |
| 2 | Inheritance | `Animal` → `Dog`/`Cat`/`Bird`; `User` → `Admin`/`Client`; `ShelterException` hierarchy |
| 3 | Polymorphism | Abstract `makeSound()`, `getCareInstructions()`, `getRole()`; polymorphic `mapRow()` in repositories |
| 4 | Data types | `int`, `BigDecimal`, `LocalDate`, `LocalDateTime`, enums, nullable `Boolean` |
| 5 | Method overloading | `User.getDisplayName()` × 3, `AuthService.createAdmin()` × 2, `AnimalService.search()` × 4, `AdoptionService.request()` × 2 |
| 6 | Access to parent | `super(...)` constructors, `super.getInfo()` extensions |
| 7 | Nested classes | `Adoption.Receipt` (static nested) |
| 8 | Packages | `model`, `enums`, `exception`, `db`, `repository`, `service`, `web`, etc. |
| 9 | Exceptions | Custom `ShelterException` hierarchy with SQLException → domain translation |
| 10 | Interfaces | `Repository<T, ID>`, `Authenticator`, `Controller`, `Html.Node` |
| 11 | Recursion | `AdoptionService.countAdoptionsRecursive()` and `walkHistory()` |
| 12 | Garbage collection | `GarbageCollectionDemo` with live heap measurements |

## Project conventions

- All JDBC queries use `PreparedStatement` (SQL injection safe)
- All JDBC resources use try-with-resources
- HTML output is escaped by default via the `Html` DSL (XSS safe)
- Passwords are hashed with BCrypt (cost 12)
- Sessions are stored server-side; only an opaque token is sent in an HttpOnly cookie
- Authorization is enforced both at the service layer (`requireAdmin()`) and the route layer (`Guards.adminOnly`)
