package org.shoulder.batch.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportLocalizeConfig {

    private String languageId;

    private String encoding;

    private String delimiter;

}
