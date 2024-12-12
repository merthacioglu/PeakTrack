-- Sample data for User entity
INSERT INTO user (username, password, name, last_name, email, phone, age, gender, height, weight, additional_notes) VALUES
                                                                                                                        ('john_doe', 'P@ssword123!', 'John', 'Doe', 'john.doe@example.com', NULL, 25, 'MALE', 180, 75, 'Prone to knee injuries; should avoid high-impact activities'),
                                                                                                                        ('jane_smith', 'S3cur3Pass!', 'Jane', 'Smith', 'jane.smith@example.com', '123-456-7890', 30, 'FEMALE', 165, 60, 'Enjoys outdoor activities; should be cautious of asthma during high pollen seasons'),
                                                                                                                        ('alex_taylor', 'MyPass#2024', 'Alex', 'Taylor', NULL, '555-123-4567', 40, 'TRANSGENDER', 170, 68, 'Prone to hypertension; should avoid heavy lifting'),
                                                                                                                        ('michael_jordan', 'J0rdan23!', 'Michael', 'Jordan', 'mjordan@example.com', NULL, 35, 'MALE', 198, 98, 'Basketball player; monitor for joint strain and overuse injuries'),
                                                                                                                        ('emily_brown', 'Em!ly2023!', 'Emily', 'Brown', 'emily.brown@example.com', '987-654-3210', 28, 'FEMALE', 160, 55, 'Yoga enthusiast; should be cautious of hypermobility in joints');


-- Sample data for Exercise entity
INSERT INTO exercise (name, description, category, muscle_group, sets, repetitions, weight) VALUES
                                                                                                ('Push-up', 'Bodyweight exercise focusing on chest and arms', 'STRENGTH', 'CHEST', 3, 15, 0),
                                                                                                ('Squat', 'Strength exercise targeting legs', 'STRENGTH', 'QUADS', 4, 12, 50),
                                                                                                ('Running', 'Cardio exercise for endurance', 'CARDIO', NULL, 1, 30, 0),
                                                                                                ('Pull-up', 'Upper body workout focusing on back and arms', 'STRENGTH', 'BACK', 3, 10, 0),
                                                                                                ('Plank', 'Core strengthening exercise', 'FLEX', 'ABS', 3, 1, 0),
                                                                                                ('Deadlift', 'Strength exercise for lower back and legs', 'STRENGTH', 'HAMSTRINGS', 3, 8, 80),
                                                                                                ('Bench Press', 'Chest exercise using weights', 'STRENGTH', 'CHEST', 4, 10, 60),
                                                                                                ('Jump Rope', 'High-intensity cardio workout', 'CARDIO', NULL, 1, 20, 0),
                                                                                                ('Bicep Curl', 'Arm exercise focusing on biceps', 'STRENGTH', 'ARMS', 3, 12, 20),
                                                                                                ('Lunges', 'Leg and glute exercise', 'STRENGTH', 'GLUTES', 4, 12, 40),
                                                                                                ('Burpees', 'Full body workout combining strength and cardio', 'CARDIO', NULL, 1, 15, 0),
                                                                                                ('Yoga', 'Flexibility and balance routine', 'FLEX', 'ABS', 1, 1, 0);

-- Sample data for Workout entity
INSERT INTO workout (name, start, duration_in_minutes, user_id) VALUES
                                                                    ('Morning Strength', '2024-12-10 06:30:00', 60, 1),
                                                                    ('Evening Cardio', '2024-12-10 18:00:00', 45, 2),
                                                                    ('Full Body Routine', '2024-12-11 07:00:00', 75, 3),
                                                                    ('Leg Day', '2024-12-12 06:00:00', 60, 4),
                                                                    ('Upper Body Strength', '2024-12-12 18:30:00', 50, 1),
                                                                    ('Yoga and Core', '2024-12-13 08:00:00', 40, 5),
                                                                    ('HIIT Session', '2024-12-13 19:00:00', 30, 2);

-- Sample data for Workout and Exercise relationship
INSERT INTO workout_exercises (workout_id, exercise_id) VALUES
-- Morning Strength: Push-up, Squat, Deadlift
(1, 1), (1, 2), (1, 6),

-- Evening Cardio: Running, Jump Rope, Burpees
(2, 3), (2, 8), (2, 11),

-- Full Body Routine: Pull-up, Squat, Bench Press, Plank
(3, 4), (3, 2), (3, 7), (3, 5),

-- Leg Day: Squat, Deadlift, Lunges
(4, 2), (4, 6), (4, 10),

-- Upper Body Strength: Push-up, Pull-up, Bench Press, Bicep Curl
(5, 1), (5, 4), (5, 7), (5, 9),

-- Yoga and Core: Yoga, Plank
(6, 12), (6, 5),

-- HIIT Session: Jump Rope, Burpees, Running
(7, 8), (7, 11), (7, 3);
