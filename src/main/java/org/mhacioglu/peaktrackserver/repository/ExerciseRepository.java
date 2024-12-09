package org.mhacioglu.peaktrackserver.repository;

import org.mhacioglu.peaktrackserver.model.Exercise;
import org.springframework.data.repository.CrudRepository;

public interface ExerciseRepository extends CrudRepository<Exercise, Long> {
}
