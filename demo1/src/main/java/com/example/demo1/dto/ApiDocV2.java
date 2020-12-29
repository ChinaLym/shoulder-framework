package com.example.demo1.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.util.Date;

/**
 * ApiDoc 测试
 *
 * @author lym
 * @see springfox.documentation.schema.ScalarType dataType
 */
@Data
@ApiModel("接口实体")
public class ApiDocV2 {

    @NotEmpty(message = "name is notnull")
    @ApiModelProperty(value = "主键", dataType = "uuid", required = true, example = "d1a27abd-56b7-44c5-838c-05e1a50809f6")
    String id;

    @Size(min = 1, max = 10)
    @NotEmpty(message = "name is notnull")
    @ApiModelProperty(value = "名称", required = true, example = "小明")
    String name;

    @Min(value = 0)
    @Max(value = 200)
    @ApiModelProperty(value = "年龄", required = false, example = "20")
    Integer age;


    @ApiModelProperty(value = "地址", example = "广州市")
    String address;

    @URL
    @ApiModelProperty(value = "头像", dataType = "url", example = "http://xxx.com/abc.jpg")
    String image;

    @Past
    @ApiModelProperty(value = "出生日期", required = true, example = "2020-1-11")
    Date birth;

    @ApiModelProperty(value = "是否启用", required = true, example = "true")
    String enable;

}
