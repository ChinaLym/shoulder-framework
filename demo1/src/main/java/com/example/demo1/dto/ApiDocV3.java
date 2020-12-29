package com.example.demo1.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

/**
 * 测试 bean 转换（DTO、BO等之间转换）
 * <p>
 * 引入了 Lombok，get/set方法不再需要写
 *
 * @author lym
 */
@Data
@ApiModel("接口实体")
public class ApiDocV3 {


    @NotEmpty(message = "name is notnull")
    @Schema(name = "主键", required = true, example = "d1a27abd-56b7-44c5-838c-05e1a50809f6")
    String id;

    @Schema(name = "名称", required = true, example = "小明", minLength = 1, maxLength = 10)
    String name;

    @Schema(name = "年龄", example = "20", minimum = "0", maximum = "200")
    Integer age;

    @Schema(name = "地址", example = "广州市", pattern = "^.*$")
    String address;

    @Schema(name = "头像", example = "http://xxx.com/abc.jpg", pattern = "https?://.*")
    String image;

    @Schema(name = "出生日期", required = true, example = "2020-1-11")
    Date birth;

    @Schema(name = "是否启用", required = true, example = "true")
    String enable;

}
