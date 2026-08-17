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

Stateless JWT authentication (`POST /auth/login`, `Authorization: Bearer <token>` on every other request). Three roles: `STUDENT`, `TEACHER`, `ADMIN`. Role checks are enforced two ways depending on how coarse the rule is:

- Coarse, role-only rules use `@PreAuthorize` on the controller (e.g. only `ADMIN` can create accounts or close an academic year, only `ADMIN`/`TEACHER` can download a graduates list).
- Ownership rules that need to compare the caller to a path variable (a student may only see their own grades/transcript, a teacher may only grade an exam for a course they're assigned to this year) are enforced in the controller/service instead, since `@PreAuthorize` alone can't express "this student ID must equal the caller's ID" without duplicating a lookup. These throw `AccessDeniedException`, which Spring Security's existing filter chain turns into a 403 the same way a `@PreAuthorize` failure would.

## Password strategy

Passwords are bcrypt-hashed, never stored or exposed in plain text. There is no self-signup: accounts are created by an admin only (`POST /admin/students`, `POST /admin/teachers`), which returns a random temporary password the admin communicates out of band. `POST /admin/users/{id}/reset-password` issues a new temporary password without needing the old one (lost-access recovery); `PATCH /users/me/password` lets any authenticated user change their own password but requires the current one.

## Year repetition and promotion

A student repeats a year when either condition holds, independently: fewer than 60 validated credits for that year, or an annual average below 10/20 (`YearRepetitionService`). Repeating does **not** move the student to a new `Promotion` — they stay on the same one, flagged `repeating` on their `StudentEnrollment` row for that academic year, and get a new `StudentEnrollment` row (new academic year, same `Promotion`) for the retake. Graduation eligibility (`GraduationService`) and rankings (`RankingService`) only consider the final, non-repeating attempt for each year — a failed attempt that was later retaken never blocks graduation.

## Database

DB is Poja-managed PostgreSQL (configured via console.poja.io, not via a manual datasource block in this repo).

## Demo dataset

The subject explicitly allows inventing all courses, students, teachers, and promotions for demonstration purposes — there is no real HEI curriculum or roster backing this data.
