package com.elevate.consultingplatform.service.impl;

import com.elevate.consultingplatform.dto.questionnaire.QuestionDto;
import com.elevate.consultingplatform.dto.questionnaire.SegmentDto;
import com.elevate.consultingplatform.dto.questionnaire.SubmissionRequest;
import com.elevate.consultingplatform.dto.questionnaire.SubmissionResponse;
import com.elevate.consultingplatform.entity.assessment.Option;
import com.elevate.consultingplatform.entity.assessment.Question;
import com.elevate.consultingplatform.entity.catalog.Segment;
import com.elevate.consultingplatform.repository.assessment.OptionRepository;
import com.elevate.consultingplatform.repository.assessment.QuestionRepository;
import com.elevate.consultingplatform.repository.catalog.SegmentRepository;
import com.elevate.consultingplatform.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionnaireServiceImpl implements QuestionnaireService {

    private final QuestionRepository questionRepository;
    private final SegmentRepository segmentRepository;
    private final OptionRepository optionRepository;

    @Override
    public List<QuestionDto> getAllQuestions() {
        return questionRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((Question q) -> Objects.requireNonNullElse(q.getOrderIndex(), Integer.MAX_VALUE))
                        .thenComparing(Question::getId))
                .map(this::toQuestionDto)
                .collect(Collectors.toList());
    }

    @Override
    public SubmissionResponse submitAnswers(SubmissionRequest request) {
        // Minimal implementation: compute a naive score as count of non-empty answers * 1
        int score = 0;
        if (request != null && request.getAnswers() != null) {
            score = (int) request.getAnswers().stream()
                    .filter(a -> a.getValue() != null && !a.getValue().isBlank())
                    .count();
        }
        return SubmissionResponse.builder()
                .submissionId(0L)
                .stage("PENDING")
                .score(score)
                .summary("Submission received.")
                .build();
    }

    @Override
    public List<SegmentDto> getSegments() {
        List<Segment> segments = segmentRepository.findAll();
        return segments.stream()
                .sorted(Comparator
                        .comparing((Segment s) -> Objects.requireNonNullElse(s.getOrderIndex(), Integer.MAX_VALUE))
                        .thenComparing(Segment::getId))
                .map(seg -> SegmentDto.builder()
                        .id(seg.getId())
                        .name(seg.getName())
                        .order(seg.getOrderIndex())
                        .questions(questionRepository.findAll().stream()
                                .filter(q -> q.getSegment() != null && Objects.equals(q.getSegment().getId(), seg.getId()))
                                .sorted(Comparator
                                        .comparing((Question q) -> Objects.requireNonNullElse(q.getOrderIndex(), Integer.MAX_VALUE))
                                        .thenComparing(Question::getId))
                                .map(this::toQuestionDto)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    private QuestionDto toQuestionDto(Question q) {
        List<Option> opts = optionRepository.findByQuestionOrderByOrderIndexAscIdAsc(q);
        String[] optionLabels = opts.stream().map(Option::getLabel).toArray(String[]::new);
        return QuestionDto.builder()
                .id(q.getId())
                .text(q.getText())
                .options(optionLabels)
                .weight(q.getWeight() != null ? q.getWeight().intValue() : null)
                .build();
    }
}
