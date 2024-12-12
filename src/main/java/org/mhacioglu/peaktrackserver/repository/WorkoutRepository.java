package org.mhacioglu.peaktrackserver.repository;

import org.mhacioglu.peaktrackserver.model.Workout;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WorkoutRepository extends CrudRepository<Workout, Long> {
    List<Workout> findAllByUserId(Long userId);
}
