DROP TABLE IF EXISTS workout_exercises;
DROP TABLE IF EXISTS workout;
DROP TABLE IF EXISTS exercise;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      username VARCHAR(255) NOT NULL,
                      password VARCHAR(255) NOT NULL,
                      name VARCHAR(255) NOT NULL,
                      last_name VARCHAR(255) NOT NULL,
                      email VARCHAR(255),
                      phone VARCHAR(50),
                      age INT CHECK (age >= 10 AND age <= 80),
                      gender VARCHAR(50) NOT NULL,
                      height INT,
                      weight INT,
                      additional_notes TEXT
);

-- Table for the Exercise entity
CREATE TABLE exercise (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          category VARCHAR(50),
                          muscle_group VARCHAR(50),
                          sets INT NOT NULL CHECK (sets >= 1),
                          repetitions INT NOT NULL CHECK (repetitions >= 1),
                          weight INT
);

-- Table for the Workout entity
CREATE TABLE workout (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         start TIMESTAMP NOT NULL,
                         duration_in_minutes INT NOT NULL,
                         user_id BIGINT,
                         FOREIGN KEY (user_id) REFERENCES user(id)
);

-- Join table for Workout and Exercise many-to-many relationship
CREATE TABLE workout_exercises (
                                   workout_id BIGINT,
                                   exercise_id BIGINT,
                                   PRIMARY KEY (workout_id, exercise_id),
                                   FOREIGN KEY (workout_id) REFERENCES workout(id),
                                   FOREIGN KEY (exercise_id) REFERENCES exercise(id)
);
