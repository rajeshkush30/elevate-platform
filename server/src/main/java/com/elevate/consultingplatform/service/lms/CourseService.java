package com.elevate.consultingplatform.service.lms;

import java.util.List;
import java.util.Map;

public interface CourseService {
    Map<String, Object> getCourseForStage(Long stageId);
    void startLesson(Long lessonId, Integer lastPositionSeconds);
    void completeLesson(Long lessonId, String evidenceUrl);
    Map<String, Object> submitQuiz(Long lessonId, List<AnswerItem> answers);

    class AnswerItem {
        public Long questionId;
        public List<Long> optionIds;
    }
}
