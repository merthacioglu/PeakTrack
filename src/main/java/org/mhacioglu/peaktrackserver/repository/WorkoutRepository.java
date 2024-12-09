package org.mhacioglu.peaktrackserver.repository;

import org.mhacioglu.peaktrackserver.model.Workout;
import org.springframework.data.repository.CrudRepository;

public interface WorkoutRepository extends CrudRepository<Workout, Long> {
}
