package com.example.demo1.config;

import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.lang.reflect.Field;
import java.util.Optional;

import static springfox.documentation.schema.Annotations.findPropertyAnnotation;
import static springfox.documentation.swagger.schema.ApiModelProperties.findApiModePropertyAnnotation;

/**
 * swagger 展示，字段排序【OpenAPI 3 中没有相关定义，可能不生效】
 *
 * @author lym
 */
//@Component
public class CustomApiModelPropertyPositionBuilder implements ModelPropertyBuilderPlugin {

    private Log log = LogFactory.getLog(getClass());

    @Override
    public boolean supports(DocumentationType delimiter) {
        return SwaggerPluginSupport.pluginDoesApply(delimiter);
    }

    @Override
    public void apply(ModelPropertyContext context) {
        Optional<BeanPropertyDefinition> beanPropertyDefinitionOpt = context.getBeanPropertyDefinition();
        Optional<ApiModelProperty> annotation = Optional.empty();
        if (context.getAnnotatedElement().isPresent()) {
            annotation = annotation.or(() -> findApiModePropertyAnnotation(context.getAnnotatedElement().get()));
        }
        if (context.getBeanPropertyDefinition().isPresent()) {
            annotation = annotation.or(() -> findPropertyAnnotation(context.getBeanPropertyDefinition().get(), ApiModelProperty.class));
        }
        if (beanPropertyDefinitionOpt.isPresent()) {
            BeanPropertyDefinition beanPropertyDefinition = beanPropertyDefinitionOpt.get();
            if (annotation.isPresent() && annotation.get().position() != 0) {
                return;
            }
            AnnotatedField field = beanPropertyDefinition.getField();
            Class<?> clazz = field.getDeclaringClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            Field declaredField;
            try {
                declaredField = clazz.getDeclaredField(field.getName());
            } catch (NoSuchFieldException | SecurityException e) {
                log.error("", e);
                return;
            }
            int indexOf = ArrayUtils.indexOf(declaredFields, declaredField);
            if (indexOf != -1) {
                context.getBuilder().position(indexOf);
            }
        }
    }
}