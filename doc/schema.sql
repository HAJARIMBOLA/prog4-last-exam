CREATE TABLE users (
    id                    uuid PRIMARY KEY,
    email                 varchar(255) NOT NULL UNIQUE,
    password_hash         varchar(255) NOT NULL,
    role                  varchar(20)  NOT NULL CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN')),
    matriculation_number  varchar(50) UNIQUE,
    first_name            varchar(255),
    last_name             varchar(255)
);

CREATE TABLE academic_years (
    id          uuid PRIMARY KEY,
    start_date  date,
    end_date    date
);

CREATE TABLE semesters (
    id                uuid PRIMARY KEY,
    academic_year_id  uuid NOT NULL REFERENCES academic_years (id),
    semester_number   integer,
    start_date        date,
    end_date          date
);
CREATE INDEX idx_semesters_academic_year_id ON semesters (academic_year_id);

CREATE TABLE promotions (
    id                          uuid PRIMARY KEY,
    label                       varchar(255),
    expected_graduation_year    integer
);

CREATE TABLE groups (
    id   uuid PRIMARY KEY,
    ref  varchar(50)
);

CREATE TABLE courses (
    id       uuid PRIMARY KEY,
    ref      varchar(50),
    title    varchar(255),
    credits  integer
);

CREATE TABLE course_tracks (
    id           uuid PRIMARY KEY,
    course_id    uuid NOT NULL REFERENCES courses (id),
    track        varchar(20) NOT NULL CHECK (track IN ('COMMON_CORE', 'EL', 'TN')),
    semester_id  uuid NOT NULL REFERENCES semesters (id)
);
CREATE INDEX idx_course_tracks_course_id ON course_tracks (course_id);
CREATE INDEX idx_course_tracks_track_semester_id ON course_tracks (track, semester_id);

CREATE TABLE course_assignments (
    id                uuid PRIMARY KEY,
    course_id         uuid NOT NULL REFERENCES courses (id),
    teacher_id        uuid NOT NULL REFERENCES users (id),
    academic_year_id  uuid NOT NULL REFERENCES academic_years (id)
);
CREATE INDEX idx_course_assignments_course_id ON course_assignments (course_id);
CREATE INDEX idx_course_assignments_course_year ON course_assignments (course_id, academic_year_id);

CREATE TABLE exams (
    id            uuid PRIMARY KEY,
    date_exam     date,
    coefficient   double precision,
    course_id     uuid NOT NULL REFERENCES courses (id)
);
CREATE INDEX idx_exams_course_id ON exams (course_id);

CREATE TABLE grade_history (
    id            uuid PRIMARY KEY,
    student_id    uuid NOT NULL REFERENCES users (id),
    exam_id       uuid NOT NULL REFERENCES exams (id),
    value         double precision,
    recorded_at   timestamp,
    recorded_by   uuid NOT NULL REFERENCES users (id)
);
CREATE INDEX idx_grade_history_student_exam ON grade_history (student_id, exam_id);
CREATE INDEX idx_grade_history_student_id ON grade_history (student_id);

CREATE TABLE group_memberships (
    id           uuid PRIMARY KEY,
    student_id   uuid NOT NULL REFERENCES users (id),
    group_id     uuid NOT NULL REFERENCES groups (id),
    date_start   date,
    date_end     date
);
CREATE INDEX idx_group_memberships_student_id ON group_memberships (student_id);

CREATE TABLE student_enrollments (
    id                 uuid PRIMARY KEY,
    student_id         uuid NOT NULL REFERENCES users (id),
    promotion_id       uuid NOT NULL REFERENCES promotions (id),
    academic_year_id   uuid NOT NULL REFERENCES academic_years (id),
    track_at_time      varchar(20) CHECK (track_at_time IN ('COMMON_CORE', 'EL', 'TN')),
    repeating          boolean NOT NULL DEFAULT false
);
CREATE INDEX idx_student_enrollments_student_id ON student_enrollments (student_id);
CREATE INDEX idx_student_enrollments_student_year ON student_enrollments (student_id, academic_year_id);
CREATE INDEX idx_student_enrollments_promotion_id ON student_enrollments (promotion_id);
CREATE INDEX idx_student_enrollments_academic_year_id ON student_enrollments (academic_year_id);
