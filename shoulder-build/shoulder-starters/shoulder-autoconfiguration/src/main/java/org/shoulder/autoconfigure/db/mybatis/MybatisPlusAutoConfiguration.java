package org.shoulder.autoconfigure.db.mybatis;

import com.baomidou.mybatisplus.core.MybatisPlusVersion;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import lombok.extern.slf4j.Slf4j;
import org.shoulder.core.constant.PageConst;
import org.shoulder.data.mybatis.config.handler.ModelMetaObjectHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisPlusConfig
 *
 * @author lym
 */
@Slf4j
@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnClass(MybatisPlusVersion.class)
public class MybatisPlusAutoConfiguration {

    /**
     * 开启分页插件
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(PaginationInterceptor.class)
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置最大单页限制数量，默认 500 条，-1 不受限制，这里改为 PageConst.MAX_PAGE_SIZE
        paginationInterceptor.setLimit(PageConst.MAX_PAGE_SIZE);
        // 若查询目标页码大于总页码，则查询第一页
        paginationInterceptor.setOverflow(false);
        // 开启 count 的 join 优化,只针对部分 left join
        paginationInterceptor.setCountSqlParser(new JsqlParserCountOptimize(true));
        return paginationInterceptor;
    }

    /**
     * 自动填充模型数据
     */
    @Bean
    @ConditionalOnMissingBean
    public ModelMetaObjectHandler modelMetaObjectHandler() {
        ModelMetaObjectHandler metaObjectHandler = new ModelMetaObjectHandler();
        log.info("ModelMetaObjectHandler [{}]", metaObjectHandler);
        return metaObjectHandler;
    }
}
