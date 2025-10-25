package com.elevate.consultingplatform.service.assessment.impl;

import com.elevate.consultingplatform.entity.assessment.AssessmentAttempt;
import com.elevate.consultingplatform.entity.assessment.Answer;
import com.elevate.consultingplatform.entity.assessment.Question;
import com.elevate.consultingplatform.service.assessment.ScoringService;
import org.springframework.stereotype.Service;

@Service
public class ScoringServiceImpl implements ScoringService {

    @Override
    public double computeTotalScore(AssessmentAttempt attempt) {
        double totalWeight = 0.0;
        double weightedScore = 0.0;
        for (Answer ans : attempt.getAnswers()) {
            Question q = ans.getQuestion();
            double w = q.getWeight() == null ? 1.0 : q.getWeight();
            totalWeight += w;
            double s = ans.getScore() != null ? ans.getScore() : parseNumeric(ans.getValue());
            weightedScore += (s * w);
        }
        if (totalWeight == 0.0) return 0.0;
        // Normalize to 0..100 if inputs are likely 0..100. If parseNumeric returned 0..10, admin should adjust weights.
        double result = weightedScore / totalWeight;
        // Clamp
        if (result < 0) result = 0;
        if (result > 100) result = 100;
        return result;
    }

    private double parseNumeric(String value) {
        if (value == null) return 0.0;
        try {
            return Double.parseDouble(value);
        } catch (Exception ignored) {
            return 0.0;
        }
    }
}
