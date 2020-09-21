package org.shoulder.crypto.negotiation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 敏感字段包装器（为了支持字段嵌套与泛型）
 *
 * @author lym
 */
@Data
@NoArgsConstructor
public class SensitiveFieldWrapper {

    private Field field;

    private boolean sensitive = true;

    private List<SensitiveFieldWrapper> internalFields = new LinkedList<>();

    public SensitiveFieldWrapper(Field field) {
        this.field = field;
    }

    public void addInternalFields(SensitiveFieldWrapper field) {
        this.internalFields.add(field);
        sensitive = false;
    }

    public void addInternalFields(List<SensitiveFieldWrapper> fields) {
        this.internalFields.addAll(fields);
        sensitive = false;
    }

    /**
     * 整理，检查值，内存压缩
     */
    public void clearedUp() {
        if (CollectionUtils.isEmpty(internalFields)) {
            sensitive = true;
            internalFields = Collections.emptyList();
        } else {
            sensitive = false;
            internalFields = new ArrayList<>(internalFields);
        }
    }
}
