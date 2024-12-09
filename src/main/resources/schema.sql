DROP TABLE IF EXISTS workout_exercises;
DROP TABLE IF EXISTS workout;
DROP TABLE IF EXISTS exercise;

CREATE TABLE IF NOT EXISTS exercise (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
   name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(255),
    muscle_group VARCHAR(255),
    sets INT NOT NULL CHECK (sets >= 1),
    repetitions INT NOT NULL CHECK (repetitions >= 1),
    weight INT
    );

CREATE TABLE IF NOT EXISTS workout (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       name VARCHAR(255) NOT NULL,
    start DATETIME NOT NULL,
    duration_in_minutes INT NOT NULL
    );

CREATE TABLE IF NOT EXISTS workout_exercises (
                                                 workout_id BIGINT NOT NULL,
                                                 exercise_id BIGINT NOT NULL,
                                                 PRIMARY KEY (workout_id, exercise_id),
    CONSTRAINT FK_workout FOREIGN KEY (workout_id) REFERENCES workout(id) ON DELETE CASCADE,
    CONSTRAINT FK_exercise FOREIGN KEY (exercise_id) REFERENCES exercise(id) ON DELETE CASCADE
    );

-- Add any necessary initial data here
