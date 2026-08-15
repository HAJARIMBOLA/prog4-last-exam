package com.example.demo.repository;

import com.example.demo.domain.CourseTrack;
import com.example.demo.domain.Track;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseTrackRepository extends JpaRepository<CourseTrack, UUID> {

  List<CourseTrack> findByCourseId(UUID courseId);

  List<CourseTrack> findByTrackAndSemesterId(Track track, UUID semesterId);
}
