package org.shoulder.validation;

import lombok.Data;
import org.shoulder.validate.annotation.Enum;
import org.springframework.validation.annotation.Validated;

/**
 * EnumTest
 *
 * @author lym
 */
public class EnumTest {


    public static void main(String[] args) {

    }

    public String acceptEnumNameDemo(@Validated DemoDTO demoDTO){
        return demoDTO.getName();
    }


    @Data
    public class DemoDTO {

        @Enum(enums={"foo", "bar"})
        private String name;

    }
}
