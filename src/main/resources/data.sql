-- Insert exercises
INSERT INTO exercise (name, description, category, muscle_group, sets, repetitions, weight)
VALUES
    ('Push Up', 'A bodyweight exercise that works the chest, shoulders, and triceps.', 'STRENGTH', 'CHEST', 3, 15, 0),
    ('Squat', 'A strength exercise that targets the quads, hamstrings, and glutes.', 'STRENGTH', 'QUADS', 3, 20, 0),
    ('Deadlift', 'A compound strength exercise targeting the back, glutes, and hamstrings.', 'STRENGTH', 'BACK', 4, 12, 80),
    ('Bench Press', 'A classic upper body exercise for building chest, shoulders, and triceps.', 'STRENGTH', 'CHEST', 4, 10, 60),
    ('Pull Up', 'A bodyweight exercise for building upper body strength, especially the back and biceps.', 'STRENGTH', 'BACK', 3, 8, 0),
    ('Lunges', 'A lower body exercise focusing on quads, hamstrings, and glutes.', 'STRENGTH', 'QUADS', 3, 15, 0),
    ('Bicep Curl', 'A strength exercise targeting the biceps.', 'STRENGTH', 'ARMS', 3, 12, 15),
    ('Tricep Dips', 'An exercise for building the triceps using body weight or added resistance.', 'STRENGTH', 'ARMS', 3, 12, 0),
    ('Leg Press', 'A machine-based exercise to target the quads, hamstrings, and glutes.', 'STRENGTH', 'QUADS', 4, 12, 100),
    ('Plank', 'A core exercise that strengthens the abs, lower back, and shoulders.', 'STRENGTH', 'ABS', 1, 60, 0),
    ('Mountain Climbers', 'A full-body workout that engages the core, legs, and arms.', 'CARDIO', 'ABS', 3, 30, 0),
    ('Running', 'An excellent cardio exercise to improve endurance and burn fat.', 'CARDIO', 'CALVES', 1, 30, 0),
    ('Jump Rope', 'A high-intensity cardio exercise that works the calves, shoulders, and arms.', 'CARDIO', 'CALVES', 3, 60, 0),
    ('Cycling', 'A cardio activity that works the legs and improves cardiovascular health.', 'CARDIO', 'QUADS', 1, 45, 0),
    ('Yoga', 'A low-impact activity that promotes flexibility and balance.', 'FLEX', 'ABS', 1, 45, 0),
    ('Balance Beam', 'An exercise designed to improve balance and core strength.', 'BALANCE', 'ABS', 1, 30, 0);

-- Insert workouts
INSERT INTO workout (name, start, duration_in_minutes)
VALUES
    ('Full Body Strength', '2024-12-01 08:00:00', 60),
    ('Cardio Endurance', '2024-12-02 07:30:00', 45),
    ('Upper Body Strength', '2024-12-03 09:00:00', 45),
    ('Leg Day', '2024-12-04 10:00:00', 60),
    ('Core and Balance', '2024-12-05 08:30:00', 45);

-- Insert workout_exercises (many-to-many relationships between workouts and exercises)
INSERT INTO workout_exercises (workout_id, exercise_id)
VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), -- Full Body Strength
    (2, 11), (2, 12), (2, 13), (2, 14), (2, 15), -- Cardio Endurance
    (3, 1), (3, 4), (3, 5), (3, 7), (3, 8), -- Upper Body Strength
    (4, 2), (4, 6), (4, 9), (4, 10), -- Leg Day
    (5, 10), (5, 15), (5, 11); -- Core and Balance
