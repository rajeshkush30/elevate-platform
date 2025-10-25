package com.elevate.consultingplatform.service.catalog.impl;

import com.elevate.consultingplatform.dto.catalog.CreateModuleRequest;
import com.elevate.consultingplatform.dto.catalog.CreateSegmentRequest;
import com.elevate.consultingplatform.dto.catalog.CreateStageRequest;
import com.elevate.consultingplatform.dto.catalog.ModuleTreeResponse;
import com.elevate.consultingplatform.entity.catalog.Module;
import com.elevate.consultingplatform.entity.catalog.Segment;
import com.elevate.consultingplatform.entity.catalog.Stage;
import com.elevate.consultingplatform.repository.catalog.ModuleRepository;
import com.elevate.consultingplatform.repository.catalog.SegmentRepository;
import com.elevate.consultingplatform.repository.catalog.StageRepository;
import com.elevate.consultingplatform.service.catalog.CatalogService;
import com.elevate.consultingplatform.mapper.CatalogTreeMapper;
import lombok.RequiredArgsConstructor;
import com.elevate.consultingplatform.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final ModuleRepository moduleRepository;
    private final SegmentRepository segmentRepository;
    private final StageRepository stageRepository;
    private final CatalogTreeMapper catalogTreeMapper;

    @Override
    @Transactional
    public Long createModule(CreateModuleRequest req) {
        Module m = Module.builder().build();
        m.setName(req.getName());
        m.setDescription(req.getDescription());
        m.setOrderIndex(req.getOrderIndex());
        m.setIsActive(req.getIsActive());
        return moduleRepository.save(m).getId();
    }

    @Override
    @Transactional
    public void updateModule(Long id, CreateModuleRequest req) {
        Module m = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));
        if (req.getName() != null) m.setName(req.getName());
        if (req.getDescription() != null) m.setDescription(req.getDescription());
        if (req.getOrderIndex() != null) m.setOrderIndex(req.getOrderIndex());
        if (req.getIsActive() != null) m.setIsActive(req.getIsActive());
        moduleRepository.save(m);
    }

    @Override
    @Transactional
    public void deleteModule(Long id) {
        moduleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Long createSegment(CreateSegmentRequest req) {
        Module module = moduleRepository.findById(req.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));
        Segment s = Segment.builder().module(module).build();
        s.setName(req.getName());
        s.setDescription(req.getDescription());
        s.setOrderIndex(req.getOrderIndex());
        s.setIsActive(req.getIsActive());
        return segmentRepository.save(s).getId();
    }

    @Override
    @Transactional
    public void updateSegment(Long id, CreateSegmentRequest req) {
        Segment s = segmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Segment not found"));
        if (req.getModuleId() != null) {
            Module module = moduleRepository.findById(req.getModuleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Module not found"));
            s.setModule(module);
        }
        if (req.getName() != null) s.setName(req.getName());
        if (req.getDescription() != null) s.setDescription(req.getDescription());
        if (req.getOrderIndex() != null) s.setOrderIndex(req.getOrderIndex());
        if (req.getIsActive() != null) s.setIsActive(req.getIsActive());
        segmentRepository.save(s);
    }

    @Override
    @Transactional
    public void deleteSegment(Long id) {
        segmentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Long createStage(CreateStageRequest req) {
        Segment segment = segmentRepository.findById(req.getSegmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Segment not found"));
        Stage st = Stage.builder().segment(segment).type(req.getType()).build();
        st.setName(req.getName());
        st.setDescription(req.getDescription());
        st.setOrderIndex(req.getOrderIndex());
        st.setIsActive(req.getIsActive());
        st.setContentUrl(req.getContentUrl());
        st.setLmsCourseId(req.getLmsCourseId());
        st.setAssessmentConfig(req.getAssessmentConfig());
        st.setAiPromptTemplate(req.getAiPromptTemplate());
        st.setDurationMinutes(req.getDurationMinutes());
        st.setMetadata(req.getMetadata());
        return stageRepository.save(st).getId();
    }

    @Override
    @Transactional
    public void updateStage(Long id, CreateStageRequest req) {
        Stage st = stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found"));
        if (req.getSegmentId() != null) {
            Segment segment = segmentRepository.findById(req.getSegmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Segment not found"));
            st.setSegment(segment);
        }
        if (req.getType() != null) st.setType(req.getType());
        if (req.getName() != null) st.setName(req.getName());
        if (req.getDescription() != null) st.setDescription(req.getDescription());
        if (req.getOrderIndex() != null) st.setOrderIndex(req.getOrderIndex());
        if (req.getIsActive() != null) st.setIsActive(req.getIsActive());
        if (req.getContentUrl() != null) st.setContentUrl(req.getContentUrl());
        if (req.getLmsCourseId() != null) st.setLmsCourseId(req.getLmsCourseId());
        if (req.getAssessmentConfig() != null) st.setAssessmentConfig(req.getAssessmentConfig());
        if (req.getAiPromptTemplate() != null) st.setAiPromptTemplate(req.getAiPromptTemplate());
        if (req.getDurationMinutes() != null) st.setDurationMinutes(req.getDurationMinutes());
        if (req.getMetadata() != null) st.setMetadata(req.getMetadata());
        stageRepository.save(st);
    }

    @Override
    @Transactional
    public void deleteStage(Long id) {
        stageRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleTreeResponse> getModuleTree() {
        List<Module> modules = moduleRepository.findAll();
        modules.sort(Comparator.comparing(m -> m.getOrderIndex() == null ? Integer.MAX_VALUE : m.getOrderIndex()));
        return modules.stream().map(catalogTreeMapper::toTree).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void reorder(com.elevate.consultingplatform.dto.catalog.ReorderRequest req) {
        if (req == null || req.getItems() == null) return;
        for (var item : req.getItems()) {
            String type = item.getType();
            if ("modules".equalsIgnoreCase(type)) {
                var entity = moduleRepository.findById(item.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + item.getId()));
                entity.setOrderIndex(item.getOrderIndex());
            } else if ("segments".equalsIgnoreCase(type)) {
                var entity = segmentRepository.findById(item.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Segment not found: " + item.getId()));
                entity.setOrderIndex(item.getOrderIndex());
            } else if ("stages".equalsIgnoreCase(type)) {
                var entity = stageRepository.findById(item.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Stage not found: " + item.getId()));
                entity.setOrderIndex(item.getOrderIndex());
            } else {
                throw new IllegalArgumentException("Unsupported type: " + type);
            }
        }
        // Flush via repositories to persist updates
        moduleRepository.flush();
        segmentRepository.flush();
        stageRepository.flush();
    }
}
