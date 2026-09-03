USE virtual_classroom;

ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NOT NULL;

-- Your existing table showed both of these foreign keys. Keep only one.
ALTER TABLE users DROP FOREIGN KEY users_ibfk_1;

-- If SHOW CREATE TABLE users still shows a second duplicate teacher FK,
-- run the following line using its exact constraint name:
-- ALTER TABLE users DROP FOREIGN KEY fk_student_teacher;

SELECT user_id, email, CHAR_LENGTH(password) AS password_length FROM users;
SHOW CREATE TABLE users;
