package org.shoulder.web.template.dictionary.validation.validator;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.shoulder.web.template.dictionary.validation.DictionaryEnumItem;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

/**
 * @author lym
 */
public class DictionaryEnumItemValidatorForVOCollection
        implements ConstraintValidator<DictionaryEnumItem, Collection<? extends DictionaryItemDTO>> {

    private DictionaryEnumItemValidatorForVO validator;

    @Override
    public void initialize(DictionaryEnumItem annotation) {
        validator = new DictionaryEnumItemValidatorForVO();
        validator.initialize(annotation);
    }

    /**
     * Checks that the trimmed string is not empty.
     *
     * @param dictionaryItemDTOCollection The dictionaryItemDTO to validate.
     * @param constraintValidatorContext  context in which the constraint is evaluated.
     * @return Returns <code>true</code> if the string is <code>null</code> or the length of <code>charSequence</code> between the
     * specified
     * <code>min</code> and <code>max</code> values (inclusive), <code>false</code> otherwise.
     */
    @Override
    public boolean isValid(Collection<? extends DictionaryItemDTO> dictionaryItemDTOCollection,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (CollectionUtils.isEmpty(dictionaryItemDTOCollection)) {
            return true;
        }
        for (DictionaryItemDTO dictionaryItemDTO : dictionaryItemDTOCollection) {
            if (!validator.isValid(dictionaryItemDTO, constraintValidatorContext)) {
                return false;
            }
        }
        return true;
    }
}
