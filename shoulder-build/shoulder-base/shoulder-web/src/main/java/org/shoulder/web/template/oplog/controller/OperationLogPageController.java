package org.shoulder.web.template.oplog.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.shoulder.core.util.ServletUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 操作日志查询 api
 *
 * @author lym
 */
@Tag(name = "OperationLogPageController", description = "操作日志-页面")
@Controller
public class OperationLogPageController {

    private String page;

    private final String apiPath;

    public OperationLogPageController(String apiPath) {
        this.apiPath = apiPath;
    }

    @ResponseBody
    @GetMapping("${shoulder.web.ext.oplog.pageUrl:/ui/oplogs/page.html}")
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
        String classPath = "classpath*:shoulder/pages/operationLogQueryPage.html.config";
        page = ServletUtil.loadResourceContent(classPath);
        page = page.replace("##OP_LOG_API_PATH##", apiPath);
        return page;
    }

}
