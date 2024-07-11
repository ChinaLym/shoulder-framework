package org.shoulder.core.dictionary;

import lombok.Data;
import org.shoulder.core.dictionary.model.DictionaryItem;

/**
 * @author lym
 */
@Data
public class MyTestDictionary implements DictionaryItem<String> {

    private String itemId;
    private String dictionaryType;
    private String name;
    private Integer displayOrder;

}
