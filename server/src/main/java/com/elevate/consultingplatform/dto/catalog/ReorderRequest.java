package com.elevate.consultingplatform.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderRequest {
    private List<ReorderRequestItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReorderRequestItem {
        private String type; // modules | segments | stages
        private Long id;
        private Integer orderIndex;
    }
}
