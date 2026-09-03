CREATE DATABASE IF NOT EXISTS virtual_classroom;

USE virtual_classroom;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS online_classes;
DROP TABLE IF EXISTS submissions;
DROP TABLE IF EXISTS assignments;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS teacher_classroom;
DROP TABLE IF EXISTS classrooms;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;


-- =========================================================
-- 1. USERS
-- =========================================================

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,

    name VARCHAR(100) NOT NULL,

    email VARCHAR(100) UNIQUE NOT NULL,

    password VARCHAR(255) NOT NULL,

    role ENUM('STUDENT', 'TEACHER') NOT NULL,

    phone VARCHAR(20) NULL,

    teacher_id INT NULL,

    reg_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_user_email (email),

    CONSTRAINT fk_student_teacher
        FOREIGN KEY (teacher_id)
        REFERENCES users(user_id)
        ON DELETE SET NULL
);


-- =========================================================
-- 2. CLASSROOMS
-- =========================================================

CREATE TABLE classrooms (
    classroom_id INT PRIMARY KEY AUTO_INCREMENT,

    class_name VARCHAR(150) NOT NULL,

    subject VARCHAR(100) NOT NULL,

    description TEXT NULL,

    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- =========================================================
-- 3. TEACHER - CLASSROOM
-- =========================================================

CREATE TABLE teacher_classroom (
    teacher_classroom_id INT PRIMARY KEY AUTO_INCREMENT,

    teacher_id INT NOT NULL,

    classroom_id INT NOT NULL,

    UNIQUE KEY uq_teacher_class (
        teacher_id,
        classroom_id
    ),

    FOREIGN KEY (teacher_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    FOREIGN KEY (classroom_id)
        REFERENCES classrooms(classroom_id)
        ON DELETE CASCADE
);


-- =========================================================
-- 4. STUDENT ENROLLMENTS
-- =========================================================

CREATE TABLE enrollments (
    enrollment_id INT PRIMARY KEY AUTO_INCREMENT,

    student_id INT NOT NULL,

    classroom_id INT NOT NULL,

    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uq_student_class (
        student_id,
        classroom_id
    ),

    FOREIGN KEY (student_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    FOREIGN KEY (classroom_id)
        REFERENCES classrooms(classroom_id)
        ON DELETE CASCADE
);


-- =========================================================
-- 5. ASSIGNMENTS
-- =========================================================

CREATE TABLE assignments (
    assignment_id INT PRIMARY KEY AUTO_INCREMENT,

    classroom_id INT NOT NULL,

    title VARCHAR(150) NOT NULL,

    description TEXT NULL,

    due_date DATE NOT NULL,

    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (classroom_id)
        REFERENCES classrooms(classroom_id)
        ON DELETE CASCADE,

    INDEX idx_assignment_class (
        classroom_id
    )
);


-- =========================================================
-- 6. SUBMISSIONS
-- =========================================================

CREATE TABLE submissions (
    submission_id INT PRIMARY KEY AUTO_INCREMENT,

    assignment_id INT NOT NULL,

    student_id INT NOT NULL,

    answer TEXT NOT NULL,

    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    marks INT NULL,

    feedback TEXT NULL,

    UNIQUE KEY uq_assign_student (
        assignment_id,
        student_id
    ),

    FOREIGN KEY (assignment_id)
        REFERENCES assignments(assignment_id)
        ON DELETE CASCADE,

    FOREIGN KEY (student_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);


-- =========================================================
-- 7. ONLINE CLASSES
-- =========================================================

CREATE TABLE online_classes (
    online_class_id INT PRIMARY KEY AUTO_INCREMENT,

    classroom_id INT NOT NULL,

    teacher_id INT NOT NULL,

    topic VARCHAR(200) NOT NULL,

    class_date DATE NOT NULL,

    start_time TIME NOT NULL,

    end_time TIME NULL,

    meeting_link VARCHAR(500) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (classroom_id)
        REFERENCES classrooms(classroom_id)
        ON DELETE CASCADE,

    FOREIGN KEY (teacher_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    INDEX idx_online_class_classroom (
        classroom_id
    ),

    INDEX idx_online_class_teacher (
        teacher_id
    ),

    INDEX idx_online_class_date (
        class_date
    )
);