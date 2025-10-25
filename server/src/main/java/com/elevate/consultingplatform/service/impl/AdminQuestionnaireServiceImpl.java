package com.elevate.consultingplatform.service.impl;

    import com.elevate.consultingplatform.dto.questionnaire.admin.*;
    import com.elevate.consultingplatform.entity.assessment.Question;
    import com.elevate.consultingplatform.entity.assessment.Option;
    import com.elevate.consultingplatform.entity.catalog.Segment;
    import com.elevate.consultingplatform.repository.assessment.QuestionRepository;
    import com.elevate.consultingplatform.repository.assessment.OptionRepository;
    import com.elevate.consultingplatform.repository.catalog.SegmentRepository;
    import com.elevate.consultingplatform.service.AdminQuestionnaireService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;

    import java.util.Comparator;
    import java.util.List;
    import java.util.Objects;
    import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    public class AdminQuestionnaireServiceImpl implements AdminQuestionnaireService {

        private final SegmentRepository segmentRepository;
        private final QuestionRepository questionRepository;
        private final OptionRepository optionRepository;

    @Override
    public List<SegmentSummaryDto> listSegments() {
        return segmentRepository.findAll().stream()
                .sorted(Comparator.comparing(s -> Objects.requireNonNullElse(s.getOrderIndex(), Integer.MAX_VALUE)))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SegmentSummaryDto createSegment(String name, Integer order) {
        Segment s = Segment.builder().name(name).orderIndex(order).build();
        Segment saved = segmentRepository.save(s);
        return toDto(saved);
    }

    @Override
    public SegmentSummaryDto updateSegment(Long id, String name, Integer order) {
        Segment s = segmentRepository.findById(id).orElseThrow();
        if (name != null) s.setName(name);
        s.setOrderIndex(order);
        return toDto(segmentRepository.save(s));
    }

    @Override
    public void deleteSegment(Long id) {
        segmentRepository.deleteById(id);
    }

    private SegmentSummaryDto toDto(Segment s) {
        return SegmentSummaryDto.builder()
                .id(s.getId())
                .name(s.getName())
                .order(s.getOrderIndex())
                .build();
    }

    // Questions
    @Override
    public List<QuestionSummaryDto> listQuestions(Long segmentId) {
        return questionRepository.findAll().stream()
                .filter(q -> q.getSegment() != null && Objects.equals(q.getSegment().getId(), segmentId))
                .sorted(Comparator
                        .comparing((Question q) -> Objects.requireNonNullElse(q.getOrderIndex(), Integer.MAX_VALUE))
                        .thenComparing(q -> Objects.requireNonNullElse(q.getId(), Long.MAX_VALUE)))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionSummaryDto createQuestion(Long segmentId, String text, Integer weight, Integer order) {
        Segment seg = segmentRepository.findById(segmentId).orElseThrow();
        Question q = Question.builder()
                .segment(seg)
                .text(text)
                .weight(weight != null ? weight.doubleValue() : null)
                .orderIndex(order)
                .build();
        return toDto(questionRepository.save(q));
    }

    @Override
    public QuestionSummaryDto updateQuestion(Long questionId, String text, Integer weight, Integer order) {
        Question q = questionRepository.findById(questionId).orElseThrow();
        if (text != null) q.setText(text);
        q.setWeight(weight != null ? weight.doubleValue() : null);
        if (order != null) q.setOrderIndex(order);
        return toDto(questionRepository.save(q));
    }

    @Override
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    private QuestionSummaryDto toDto(Question q) {
        return QuestionSummaryDto.builder()
                .id(q.getId())
                .segmentId(q.getSegment() != null ? q.getSegment().getId() : null)
                .text(q.getText())
                .weight(q.getWeight() != null ? q.getWeight().intValue() : null)
                .order(q.getOrderIndex())
                .build();
    }

    // Options
    @Override
    public List<OptionSummaryDto> listOptions(Long questionId) {
        Question q = questionRepository.findById(questionId).orElseThrow();
        return optionRepository.findByQuestionOrderByOrderIndexAscIdAsc(q).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public OptionSummaryDto createOption(Long questionId, String label, String value, Integer order) {
        Question q = questionRepository.findById(questionId).orElseThrow();
        Option opt = Option.builder()
                .question(q)
                .label(label)
                .value(value)
                .orderIndex(order)
                .build();
        return toDto(optionRepository.save(opt));
    }

    @Override
    public OptionSummaryDto updateOption(Long optionId, String label, String value, Integer order) {
        Option opt = optionRepository.findById(optionId).orElseThrow();
        if (label != null) opt.setLabel(label);
        if (value != null) opt.setValue(value);
        if (order != null) opt.setOrderIndex(order);
        return toDto(optionRepository.save(opt));
    }

    @Override
    public void deleteOption(Long optionId) {
        optionRepository.deleteById(optionId);
    }

    private OptionSummaryDto toDto(Option o) {
        return OptionSummaryDto.builder()
                .id(o.getId())
                .questionId(o.getQuestion() != null ? o.getQuestion().getId() : null)
                .label(o.getLabel())
                .value(o.getValue())
                .order(o.getOrderIndex())
                .build();
    }

    // Reorder bulk operations
    @Override
    public void reorderSegments(java.util.List<com.elevate.consultingplatform.dto.questionnaire.admin.ReorderItemDto> items) {
        if (items == null) return;
        for (var it : items) {
            if (it.getId() == null) continue;
            segmentRepository.findById(it.getId()).ifPresent(seg -> {
                seg.setOrderIndex(it.getOrder());
                segmentRepository.save(seg);
            });
        }
    }

    @Override
    public void reorderQuestions(Long segmentId, java.util.List<com.elevate.consultingplatform.dto.questionnaire.admin.ReorderItemDto> items) {
        if (items == null) return;
        for (var it : items) {
            if (it.getId() == null) continue;
            questionRepository.findById(it.getId()).ifPresent(q -> {
                if (q.getSegment() != null && java.util.Objects.equals(q.getSegment().getId(), segmentId)) {
                    q.setOrderIndex(it.getOrder());
                    questionRepository.save(q);
                }
            });
        }
    }

    @Override
    public void reorderOptions(Long questionId, java.util.List<com.elevate.consultingplatform.dto.questionnaire.admin.ReorderItemDto> items) {
        if (items == null) return;
        Question q = questionRepository.findById(questionId).orElseThrow();
        for (var it : items) {
            if (it.getId() == null) continue;
            optionRepository.findById(it.getId()).ifPresent(o -> {
                if (o.getQuestion() != null && Objects.equals(o.getQuestion().getId(), q.getId())) {
                    o.setOrderIndex(it.getOrder());
                    optionRepository.save(o);
                }
            });
        }
    }
}
