package org.shoulder.data.mybatis.template;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * mybatis 代码生成
 *
 * @author lym
 */
public class MybatisGenerator {
    public static void generate(String jdbcUrl, String dbUser, String dbPwd, String packagePrefix, String author, String tableName,
                                String tableNamePrefix) {
        //修改成本地项目路径
        String basePath = System.getProperty("user.dir");
        Map<OutputFile, String> filePathMap = new HashMap<>(10);
        filePathMap.put(OutputFile.xml, basePath + "/app/common/dal/src/main/resources/mapper");
        filePathMap.put(OutputFile.mapper,
            basePath + "/app/common/dal/src/main/java/" + packagePrefix.replace(".", "/") + "/common/dal/mybatis/mapper");
        filePathMap.put(OutputFile.entity,
            basePath + "/app/common/dal/src/main/java/" + packagePrefix.replace(".", "/") + "/common/dal/mybatis/model");
        filePathMap.put(OutputFile.serviceImpl,
            basePath + "/app/common/dal/src/main/java/" + packagePrefix.replace(".", "/") + "/common/dal/repository/impl");
        filePathMap.put(OutputFile.service,
            basePath + "/app/common/dal/src/main/java/" + packagePrefix.replace(".", "/") + "/common/dal/repository");
        FastAutoGenerator.create(jdbcUrl, dbUser, dbPwd)
            .globalConfig(builder -> builder.author(author))
            .dataSourceConfig(builder -> builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                if (typeCode == Types.SMALLINT) {
                    // 自定义类型转换
                    return DbColumnType.INTEGER;
                }
                if (typeCode == Types.TINYINT) {
                    // 自定义类型转换
                    return DbColumnType.INTEGER;
                }
                if (typeCode == Types.TIMESTAMP) {
                    return DbColumnType.DATE;
                }
                return typeRegistry.getColumnType(metaInfo);

            }))
            //配置包名
            .packageConfig(builder -> {
                builder.parent(packagePrefix + ".common.dal")
                    .entity("mybatis.model")
                    .mapper("mybatis.mapper")
                    .service("repository")
                    .serviceImpl("repository.impl")
                    .pathInfo(filePathMap);
            })
            .strategyConfig(builder -> builder
                .addInclude(tableName)
                .addTablePrefix(tableNamePrefix)
                // -----service策略配置-----
                .serviceBuilder()
                .formatServiceFileName("%sRepository")
                .formatServiceImplFileName("%sRepositoryImpl")
                // -----实体类策略配置-----
                .entityBuilder()
                .enableFileOverride()
                // 主键策略,非自增可注释
                .idType(IdType.AUTO)
                //开启lombok
                .enableLombok()
                // 属性加上注解说明
                .enableTableFieldAnnotation()
                .convertFileName(entityName -> String.format("%sDO", entityName))
                .enableColumnConstant()
                // -----mapper策略配置-----
                .mapperBuilder()
                .formatMapperFileName("%sDAO")
                .formatXmlFileName("%sDAO"))
            // 使用Freemarker引擎模板，默认的是Velocity引擎模板
            .templateEngine(new FreemarkerTemplateEngine())
            .execute();
    }
}
