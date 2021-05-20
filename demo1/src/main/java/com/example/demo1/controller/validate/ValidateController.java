package com.example.demo1.controller.validate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.validate.annotation.Enum;
import org.shoulder.validate.groups.Update;
import org.shoulder.web.validate.ValidateRuleEndPoint;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * 测试上传文件，合法校验
 * <a href="http://localhost:8080/" />
 * 动态校验规则 <a href="http://localhost:8080/api/v1/validate/rule?method=get&uri=/validate/jsr/1" />
 * 动态校验规则 <a href="http://localhost:8080/api/v1/validate/rule/validate/jsr/1?method=get" />
 * <p>
 * 动态校验规则 <a href="http://localhost:8080/api/v1/validate/rule?method=get&uri=/validate/jsr/2" />
 * 动态校验规则 <a href="http://localhost:8080/api/v1/validate/rule?method=get&uri=/validate/jsr/3" />
 *
 * @author lym
 * @see ValidateRuleEndPoint 动态获取校验规则
 */
@Validated
@RestController
@RequestMapping("validate")
public class ValidateController {


    /**
     * 正常写法举例，框架不会嵌套包装
     */
    @RequestMapping("0")
    public BaseResult<String> notRecommended() {
        BaseResult<String> response = new BaseResult<>();
        response.setCode("0");
        response.setMsg("msg");
        response.setData("data");
        return response;
    }

    /**
     * 正常写法举例
     * http://localhost:8080/validate/1?value=x
     */
    @RequestMapping("1")
    public String caseEnum(@Enum(enums = {"abc", "def"}) String value) {
        System.out.println(value);
        return "ok";
    }


    /**
     * 正常写法举例
     * http://localhost:8080/validate/jsr/1?value=x
     */
    @RequestMapping("jsr/1")
    public String caseJsr(@Validated @Valid @NotNull @NotBlank String value) {
        System.out.println(value);
        return "ok";
    }

    /**
     * POST http://localhost:8080/validate/jsr/1?value=x
     * 动态校验规则 <a href="http://localhost:8080/api/v1/validate/rule?method=get&uri=/validate/jsr/2" />
     */
    @RequestMapping("jsr/2")
    public String caseJsr2(@Validated @Valid @NotBlank User user) {
        System.out.println(user);
        return "ok";
    }

    /**
     * POST http://localhost:8080/validate/jsr/1?value=x
     * 动态校验规则 <a href="http://localhost:8080/api/v1/validate/rule?method=get&uri=/validate/jsr/3" />
     */
    @RequestMapping("jsr/3")
    @Validated(Update.class)
    public String caseJsr3(@Validated @NotBlank User user) {
        System.out.println(user);
        return "ok";
    }

    /**
     * 自定义类型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        @NotEmpty
        private String id;

        @Min(0)
        @Max(66)
        private Integer age;

        @Pattern(regexp = "\\w+")
        private String name;

        @Pattern(regexp = "\\d+", groups = Update.class)
        private String name2;
    }

}
