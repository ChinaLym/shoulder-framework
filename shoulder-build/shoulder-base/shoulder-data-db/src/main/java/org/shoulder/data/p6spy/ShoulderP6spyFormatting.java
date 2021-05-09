package org.shoulder.data.p6spy;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.util.StringUtils;

/**
 * 打印日志时打印 trace、租户、用户、链接、sql、用时
 * 将类全限定名放到配置文件中
 *
 * @author lym
 */
public class ShoulderP6spyFormatting implements MessageFormattingStrategy {

    public static final String REGX = "[\\s]+";

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category,
                                String prepared, String sql, String url) {
        return StringUtils.isNotBlank(sql) ?
                StrUtil.format(" tenant: {} userId: {} \n Consume Time：{} ms {} \n url: {} \n Execute SQL：{} \n",
                        AppContext.getTenantCode(), AppContext.getUserId(), elapsed, now,
                        url, sql.replaceAll(REGX, StringPool.SPACE)) : "";
    }
}