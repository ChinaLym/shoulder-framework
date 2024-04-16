package org.shoulder.web.template.dictionary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.shoulder.core.dictionary.spi.DictionaryEnumStore;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * 枚举型字典接口-默认实现
 * http://localhost:8080/api/v1/dictionary/type/all
 * http://localhost:8080/ui/dictionary/page.html
 *
 * 页面使用了 bootstrap，下面是一些 bootstraps 技术参考和说明
 * https://bootstrapshuffle.com/cn/classes
 * https://www.w3schools.com/bootstrap4/bootstrap_colors.asp
 *
 * @author lym
 */
@Tag(name = "DictionaryEnumController", description = "枚举字典-类型查询(只读)")
@RestController
public class DictionaryEnumController implements DictionaryEnumQueryController {

    /**
     * 字典枚举存储
     */
    private final DictionaryEnumStore dictionaryEnumStore;

    private String page;

    public DictionaryEnumController(DictionaryEnumStore dictionaryEnumStore) {
        this.dictionaryEnumStore = dictionaryEnumStore;
    }

    /**
     * 查询所有支持的字典项名称 【低频接口】
     *
     * @return 查询结果
     */
    @Parameters({
            @Parameter(name = "dictionaryType", description = "字典类型"),
    })
    @Operation(summary = "查询所有支持的字典项名称", description = "查询所有支持的字典项名称")
    @RequestMapping(value = "${shoulder.web.ext.dictionary.apiPath:/api/v1/dictionary}/type/all", method = {RequestMethod.POST, RequestMethod.GET})
    public BaseResult<ListResult<String>> allTypes() {
        Collection<String> allTypeNames = dictionaryEnumStore.listAllTypeNames();
        return BaseResult.success(allTypeNames);
    }

    @SkipResponseWrap
    @GetMapping("${shoulder.web.ext.dictionary.pageUrl:/ui/dictionary/page.html}")
    public String uiPage(HttpServletRequest request) {
        String pageAjaxHost = request.getRequestURL().toString()
                .replace(request.getRequestURI(), "");
        return loadPage(pageAjaxHost);
    }

    private synchronized String loadPage(String pageAjaxHost) {
        if (page != null) {
            return page;
        }
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String classPath = "classpath*:shoulder/dictionary/dictionaryEnums.html.config";
        try {
            Resource[] resources = resolver.getResources(classPath);
            if (resources.length > 0) {
                Resource resource = resources[0];
                pageAjaxHost = pageAjaxHost.endsWith("/") ? pageAjaxHost.subSequence(0, pageAjaxHost.length() - 1).toString() : pageAjaxHost;
                page = resource.getContentAsString(StandardCharsets.UTF_8).replaceFirst("CONFIG_PAGE_HOST", pageAjaxHost);
            }
        } catch (IOException e) {
            page = "";
        }
        return page;
    }


}
