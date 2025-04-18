package org.shoulder.batch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchActivityBlock {
    public static final String SERIAL = "SERIAL";
    public static final String PARALLEL = "PARALLEL";
    // SERIAL / PARALLEL
    private String type;

    private List list;
}