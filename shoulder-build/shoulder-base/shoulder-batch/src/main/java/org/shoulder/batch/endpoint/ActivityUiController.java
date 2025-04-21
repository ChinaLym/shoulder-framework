package org.shoulder.batch.endpoint;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.shoulder.core.util.ServletUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Tag(name = "ActivityController", description = "自定义流程进度页面")
@Controller
public class ActivityUiController {

    private String page;

    private final String apiPath;

    public ActivityUiController(String apiPath) {
        this.apiPath = apiPath;
    }

    @ResponseBody
    @GetMapping("${shoulder.batch.activity.pageUrl:/ui/activities/page.html}")
    public String uiPage(HttpServletRequest request) {
        String pageAjaxHost = request.getRequestURL().toString()
                .replace(request.getRequestURI(), "");
        pageAjaxHost = pageAjaxHost.endsWith("/") ? pageAjaxHost.subSequence(0, pageAjaxHost.length() - 1).toString() : pageAjaxHost;
        page = loadPage();
        return page.replaceFirst("SHOULDER_PAGE_HOST", pageAjaxHost);
    }

    private synchronized String loadPage() {
        if (page != null) {
            return page;
        }
        String classPath = "classpath*:shoulder/pages/activityPage.html.config";
        page = ServletUtil.loadResourceContent(classPath);
        page = page.replace("##BATCH_ACTIVITIES_API_PATH##", apiPath);
        return page;
    }

}
