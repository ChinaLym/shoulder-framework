package org.shoulder.web.template.dictionary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.shoulder.core.dictionary.spi.DictionaryEnumStore;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.core.util.ServletUtil;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * 枚举型字典接口-默认实现
 * http://localhost:8080/api/v1/dictionaries/types/listAll
 * http://localhost:8080/ui/dictionaries/page.html
 * <p>
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

    private final String apiPath;

    public DictionaryEnumController(DictionaryEnumStore dictionaryEnumStore, String apiPath) {
        this.dictionaryEnumStore = dictionaryEnumStore;
        this.apiPath = apiPath;
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
    @RequestMapping(value = "${shoulder.web.ext.dictionary.path:/api/v1/dictionaries}/types/listAll", method = {RequestMethod.POST, RequestMethod.GET})
    public BaseResult<ListResult<String>> allTypes() {
        Collection<String> allTypeNames = dictionaryEnumStore.listAllTypeNames();
        return BaseResult.success(allTypeNames);
    }

    @SkipResponseWrap
    @GetMapping("${shoulder.web.ext.dictionary.pageUrl:/ui/dictionaries/page.html}")
    public String uiPage(HttpServletRequest request) {
        String pageAjaxHost = request.getRequestURL().toString()
                .replace(request.getRequestURI(), "");
        pageAjaxHost = pageAjaxHost.endsWith("/") ? pageAjaxHost.subSequence(0, pageAjaxHost.length() - 1).toString() : pageAjaxHost;
        page = loadPage();
        return page.replace("SHOULDER_PAGE_HOST", pageAjaxHost);
    }

    private synchronized String loadPage() {
        if (page != null) {
            return page;
        }
        String classPath = "classpath*:shoulder/pages/dictionaryQueryPage.html.config";
        page = ServletUtil.loadResourceContent(classPath);
        page = page.replace("##DICTIONARY_API_PATH##", apiPath);
        return page;
    }

}
