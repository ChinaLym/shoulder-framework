package org.shoulder.web.template.dictionary.validation.validator;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.shoulder.web.template.dictionary.validation.DictionaryEnumItem;

/**
 * 枚举接口校验器-校验 DictionaryItemDTO
 *
 * @author lym
 */
public class DictionaryEnumItemValidatorForDTO implements ConstraintValidator<DictionaryEnumItem, DictionaryItemDTO> {

    private DictionaryEnumItemValidatorForCharSequence validator;

    @Override
    public void initialize(DictionaryEnumItem annotation) {
        validator = new DictionaryEnumItemValidatorForCharSequence();
        validator.initialize(annotation);
    }

    /**
     * Checks that the trimmed string is not empty.
     *
     * @param dictionaryItemDTO          The dictionaryItemDTO to validate.
     * @param constraintValidatorContext context in which the constraint is evaluated.
     * @return Returns <code>true</code> if the string is <code>null</code> or the length of <code>charSequence</code> between the
     * specified
     * <code>min</code> and <code>max</code> values (inclusive), <code>false</code> otherwise.
     */
    @Override
    public boolean isValid(DictionaryItemDTO dictionaryItemDTO, ConstraintValidatorContext constraintValidatorContext) {
        if (dictionaryItemDTO == null) {
            return true;
        }
        String code = dictionaryItemDTO.getCode();
        return validator.isValid(code, constraintValidatorContext);
    }
}
