package com.example.demo1.controller.validate;

import org.shoulder.core.util.RegexpUtils;
import org.shoulder.validate.annotation.FileType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 测试上传文件，合法校验
 * <a href="http://localhost:8080/" />
 *
 * @author lym
 */
@Validated
@RestController
@RequestMapping("validate/file")
public class FileUploadController {

    /**
     * 用于校验文件名可选格式 / 禁止格式
     */

    private String allowNamePattern = "";

    private String forbiddenNamePattern = "";


    /**
     * 正常写法举例，框架不会嵌套包装
     * 注意表单中参数名也需要是 'uploadFile' 或添加 @Param 注解
     */
    @RequestMapping("0")
    public String notRecommended(MultipartFile uploadFile) throws IOException {
        if (uploadFile == null) {
            System.out.println("fileName: null");
            return "0";
        }
        String fileName = uploadFile.getOriginalFilename();

        // 校验文件名后缀是否合法
        fileName.endsWith(".png");

        // 正则校验文件名必须为给定的格式
        boolean onlyAllowPattern = RegexpUtils.matches(fileName, allowNamePattern);
        if (!onlyAllowPattern) {
            // 省略每种校验失败组装返回值结果、记录日志...

        }

        // 正则校验文件名禁止包含特殊字符
        boolean noForbiddenPattern = !RegexpUtils.matches(fileName, forbiddenNamePattern);
        if (!noForbiddenPattern) {
            // 省略每种校验失败组装返回值结果、记录日志...

        }
        // 校验文件头
        // 从上传文件的 inputStream 中读取前 x 个字节（具体字节数与类型相关）
        uploadFile.getInputStream();
        // 比较正确的文件头

        if (!onlyAllowPattern) {
            // 省略每种校验失败组装返回值结果、记录日志...

        }

        // 校验文件大小
        long maxSize_1MB = 1024 * 1024;
        boolean sizeOk = uploadFile.getSize() < maxSize_1MB;
        if (!sizeOk) {
            // 省略每种校验失败组装返回值结果、记录日志...

        }

        // 你的业务代码
        System.out.println("fileName: " + fileName);
        return "0";
    }


    // -------------------------------- 使用框架简化代码 --------------------------------

    /**
     * 框架自动校验
     * 只需一个注解 @FileType
     */
    @RequestMapping("1")
    public String case1(@FileType(allowSuffix = "png", maxSize = "10MB") MultipartFile uploadFile) {
        // 你的业务代码 ...

        return uploadFile == null ? "null" : uploadFile.getOriginalFilename();
    }

    /**
     * 框架自动校验
     * 只需一个注解 @FileType
     */
    @RequestMapping("2")
    public String case2(@FileType(allowSuffix = {"yml", "properties"}, maxSize = "1MB") MultipartFile uploadFile) {
        // 你的业务代码 ...

        return uploadFile == null ? "null" : uploadFile.getOriginalFilename();
    }

}
