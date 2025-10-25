package com.elevate.consultingplatform.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleTreeResponse {
    private Long id;
    private String name;
    private String description;
    private Integer orderIndex;
    private Boolean isActive;

    @Builder.Default
    private List<SegmentNode> segments = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SegmentNode {
        private Long id;
        private String name;
        private String description;
        private Integer orderIndex;
        private Boolean isActive;
        @Builder.Default
        private List<StageNode> stages = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageNode {
        private Long id;
        private String name;
        private String description;
        private Integer orderIndex;
        private Boolean isActive;
        private String type;
        private String contentUrl;
        private String lmsCourseId;
    }
}
