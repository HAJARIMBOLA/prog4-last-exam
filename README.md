# POJA - HEI Student Grades Management

Spring Boot application on AWS Lambda + SnapStart (Poja) managing HEI students' grades over a 3-year path (EL or TN track, chosen after a shared common-core year 1).

## Architecture: domain/model separation

JPA `@Entity` classes and the classes exposed through REST/Thymeleaf are strictly separated, in two different packages:

- `com.example.demo.domain` - JPA entities. Persistence-only, never returned by a controller. Carries lazy-loaded relations, database constraints, and is the only layer aware of Hibernate/JPA.
- `com.example.demo.model` - plain DTOs/records exposed through the API. No JPA annotations, no lazy-loaded relations, no password/credential fields, no persistence concerns.

Every entity has an explicit, manual mapper in `com.example.demo.mapper` that converts between the two. Controllers only ever see `model` types; they never receive or return a `domain` entity directly.

## Package layout

- `domain` - JPA entities.
- `model` - API-facing DTOs/records.
- `mapper` - manual entity <-> DTO mappers.
- `repository` - Spring Data JPA repositories.
- `service` - business logic.
- `service.event` - async event consumers (Poja Workers).
- `security` - JWT authentication (filter, token service, user details, security config).
- `endpoint.rest.controller` - REST controllers.
- `endpoint.event.model` - Poja async events.

## Auth

Stateless JWT authentication. Passwords are bcrypt-hashed, never stored or exposed in plain text. Accounts are created by an admin only (no self-signup); `POST /admin/users/{id}/reset-password` issues a new temporary password without needing the old one.

## Database

DB is Poja-managed PostgreSQL (configured via console.poja.io, not via a manual datasource block in this repo).
