package com.example.demo.testdata;

import com.example.demo.domain.Course;
import java.util.UUID;

public class CourseTestDataBuilder {

  private UUID id = UUID.randomUUID();
  private String ref = "ALG101";
  private String title = "Algorithms";
  private int credits = 4;

  public static CourseTestDataBuilder aCourse() {
    return new CourseTestDataBuilder();
  }

  public CourseTestDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  public CourseTestDataBuilder withRef(String ref) {
    this.ref = ref;
    return this;
  }

  public CourseTestDataBuilder withTitle(String title) {
    this.title = title;
    return this;
  }

  public CourseTestDataBuilder withCredits(int credits) {
    this.credits = credits;
    return this;
  }

  public Course build() {
    return new Course(id, ref, title, credits);
  }
}
