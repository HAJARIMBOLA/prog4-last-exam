package com.example.demo.service;

import com.example.demo.domain.Track;
import com.example.demo.exception.InvalidCreditStructureException;
import com.example.demo.repository.CourseTrackRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CreditStructureValidationService {

  public static final int REQUIRED_CREDITS_PER_SEMESTER = 30;
  public static final int REQUIRED_CREDITS_PER_YEAR = 2 * REQUIRED_CREDITS_PER_SEMESTER;

  private final CourseTrackRepository courseTrackRepository;

  public void assertAddingCourseWithinSemesterCap(
      Track track, UUID semesterId, int additionalCredits) {
    var projectedCredits = sumCreditsForTrackAndSemester(track, semesterId) + additionalCredits;
    if (projectedCredits > REQUIRED_CREDITS_PER_SEMESTER) {
      throw new InvalidCreditStructureException(
          "Track "
              + track
              + " semester "
              + semesterId
              + " would reach "
              + projectedCredits
              + " credits, above the required "
              + REQUIRED_CREDITS_PER_SEMESTER);
    }
  }

  public void validateCompleteYearStructure(int semester1Credits, int semester2Credits) {
    if (semester1Credits != REQUIRED_CREDITS_PER_SEMESTER
        || semester2Credits != REQUIRED_CREDITS_PER_SEMESTER) {
      throw new InvalidCreditStructureException(
          "A track's yearly structure must have exactly "
              + REQUIRED_CREDITS_PER_SEMESTER
              + " credits per semester ("
              + REQUIRED_CREDITS_PER_YEAR
              + " per year), got "
              + semester1Credits
              + "/"
              + semester2Credits);
    }
  }

  private int sumCreditsForTrackAndSemester(Track track, UUID semesterId) {
    return courseTrackRepository.findByTrackAndSemesterId(track, semesterId).stream()
        .mapToInt(courseTrack -> courseTrack.getCourse().getCredits())
        .sum();
  }
}
