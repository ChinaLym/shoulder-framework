package org.shoulder.web.template.dictionary;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.core.util.StringUtils;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * 枚举型字典接口-默认实现
 * http://localhost:8080/api/v1/dictionary/allTypes
 * http://localhost:8080/api/v1/dictionary/page
 *
 * 页面使用了 bootstrap，下面是一些 bootstraps 技术参考和说明
 * https://bootstrapshuffle.com/cn/classes
 * https://www.w3schools.com/bootstrap4/bootstrap_colors.asp
 *
 * @author lym
 */
@RestController
@RequestMapping(value = "${shoulder.web.ext.dictionary.path:/api/v1/dictionary}")
public class DictionaryEnumController implements DictionaryController {

    /**
     * 字典枚举存储
     */
    private final DictionaryEnumStore dictionaryEnumStore;

    @Value("${shoulder.web.ext.dictionary.enum.page.host:http://localhost:8080}")
    private String pageHost;

    private String page;

    public DictionaryEnumController(DictionaryEnumStore dictionaryEnumStore) {
        this.dictionaryEnumStore = dictionaryEnumStore;
    }

    /**
     * 查询所有支持的字典项名称 【低频接口】
     *
     * @return 查询结果
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dictionaryType", value = "字典类型", dataType = "String", paramType = "path"),
    })
    @ApiOperation(value = "查询所有支持的字典项名称", notes = "查询所有支持的字典项名称")
    @GetMapping("/allTypes")
    public BaseResult<ListResult<String>> allTypes() {
        Collection<String> allTypeNames = dictionaryEnumStore.listAllTypeNames();
        return BaseResult.success(allTypeNames);
    }

    @SkipResponseWrap
    @GetMapping("/page")
    public String hello() {
        if (StringUtils.isNoneBlank(pageHost)) {
            return loadPage();
        } else {
            return "";
        }
    }

    private synchronized String loadPage() {
        if (page != null) {
            return page;
        }
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String classPath = "classpath*:shoulder/dictionary/dictionaryEnums.html.config";
        try {
            Resource[] resources = resolver.getResources(classPath);
            if (resources.length > 0) {
                Resource resource = resources[0];
                pageHost = pageHost.endsWith("/") ? pageHost.subSequence(0, pageHost.length() - 1).toString() : pageHost;
                page = resource.getContentAsString(StandardCharsets.UTF_8).replaceFirst("CONFIG_PAGE_HOST", pageHost);
            }
        } catch (IOException e) {
            page = "";
        }
        return page;
    }


}
