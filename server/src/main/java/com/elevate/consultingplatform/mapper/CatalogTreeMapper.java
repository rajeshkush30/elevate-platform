package com.elevate.consultingplatform.mapper;

import com.elevate.consultingplatform.dto.catalog.ModuleTreeResponse;
import com.elevate.consultingplatform.entity.catalog.Module;
import com.elevate.consultingplatform.entity.catalog.Segment;
import com.elevate.consultingplatform.entity.catalog.Stage;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class CatalogTreeMapper {

    public ModuleTreeResponse toTree(Module m) {
        ModuleTreeResponse res = ModuleTreeResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .description(m.getDescription())
                .orderIndex(m.getOrderIndex())
                .isActive(m.getIsActive())
                .build();
        var segs = m.getSegments();
        if (segs == null || segs.isEmpty()) {
            res.setSegments(Collections.emptyList());
        } else {
            segs.sort(ordering());
            res.setSegments(segs.stream().map(this::toSegmentNode).collect(Collectors.toList()));
        }
        return res;
    }

    private ModuleTreeResponse.SegmentNode toSegmentNode(Segment s) {
        var node = ModuleTreeResponse.SegmentNode.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .orderIndex(s.getOrderIndex())
                .isActive(s.getIsActive())
                .build();
        var stages = s.getStages();
        if (stages == null || stages.isEmpty()) {
            node.setStages(Collections.emptyList());
        } else {
            stages.sort(ordering());
            node.setStages(stages.stream().map(this::toStageNode).collect(Collectors.toList()));
        }
        return node;
    }

    private ModuleTreeResponse.StageNode toStageNode(Stage t) {
        return ModuleTreeResponse.StageNode.builder()
                .id(t.getId())
                .name(t.getName())
                .description(t.getDescription())
                .orderIndex(t.getOrderIndex())
                .isActive(t.getIsActive())
                .type(t.getType() != null ? t.getType().name() : null)
                .contentUrl(t.getContentUrl())
                .lmsCourseId(t.getLmsCourseId())
                .build();
    }

    private Comparator<? super com.elevate.consultingplatform.common.HierarchyNode> ordering() {
        return Comparator.comparing(n -> n.getOrderIndex() == null ? Integer.MAX_VALUE : n.getOrderIndex());
    }
}
