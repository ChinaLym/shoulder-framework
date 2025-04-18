package org.shoulder.batch.dto;

import lombok.Data;

import java.util.Map;

@Data
public class BatchActivityDTO {
    private String id;
    private String icon;
    private String title;
    private Map<String, Object> ext;
    private boolean endStep;
}