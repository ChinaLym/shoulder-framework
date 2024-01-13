package org.shoulder.web.template.dictionary.validation.validator;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

/**
 * 支持 Pattern 集合校验
 *
 * @author lym
 */
public class PatternValidatorForCharCollection
        implements ConstraintValidator<Pattern, Collection<? extends CharSequence>> {

    private PatternValidator validator;

    @Override
    public void initialize(Pattern annotation) {
        validator = new PatternValidator();
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
    public boolean isValid(Collection<? extends CharSequence> charCollection,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (CollectionUtils.isEmpty(charCollection)) {
            return true;
        }
        for (CharSequence str : charCollection) {
            if (!validator.isValid(str, constraintValidatorContext)) {
                return false;
            }
        }
        return true;
    }
}
