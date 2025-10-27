package com.elevate.consultingplatform.integration.mock;

import com.elevate.consultingplatform.integration.AIClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Profile({"mock", "default"})
public class MockAIClient implements AIClient {
    @Override
    public PreScoreResponse preScore(PreScoreRequest req) {
        return new PreScoreResponse(0.4, "StartUp", "Heuristic based on intents", List.of("EARLY"));
    }

    @Override
    public StageResponse determineStage(StageRequest req) {
        return new StageResponse("Grow", 68.5, "Rule: revenue + team size", 0.8);
    }

    @Override
    public SummaryResponse generateSummary(SummaryRequest req) {
        return new SummaryResponse("You are in Grow stage. Focus on systems and talent.", List.of("Hire sales ops", "Document SOPs"));
    }

    @Override
    public FinalConsultationResponse finalConsultation(FinalConsultationRequest req) {
        return new FinalConsultationResponse("Executive summary...\nRecommendations...", List.of(Map.of("title", "Capital", "items", List.of("Improve runway"))));
    }
}
