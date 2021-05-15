package com.example.demo1.config;

import com.example.demo1.enums.DictionaryTestEnum;
import org.shoulder.autoconfigure.web.DictionaryEnumRepositoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动生成枚举接口，方便下拉框的开发
 *
 * @author lym
 */
@Configuration
public class DictionaryEnumConfiguration {

    /**
     * <a href="http://localhost:8080/api/v1/dictionary/allTypes"/>
     * <a href="http://localhost:8080/api/v1/dictionary/queryOne/DictionaryTestEnum"/>
     * POST! <a href="http://localhost:8080/api/v1/dictionary/DictionaryTestEnum"/>
     *
     * @return c
     * @see org.shoulder.web.template.dictionary.DictionaryController
     */
    @Bean
    DictionaryEnumRepositoryCustomizer dictionaryEnumRepositoryCustomizer() {
        return repo -> {
            repo.register(DictionaryTestEnum.class);
            //repo.register(DictionaryTestEnum2.class);
        };
    }

}
