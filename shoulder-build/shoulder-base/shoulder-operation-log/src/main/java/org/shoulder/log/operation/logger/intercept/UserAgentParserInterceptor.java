package org.shoulder.log.operation.logger.intercept;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import org.shoulder.log.operation.logger.OperationLoggerInterceptor;
import org.shoulder.log.operation.model.OperationLogDTO;

/**
 * 根据 terminal info 解析日志格式
 *
 * @author lym
 */
public class UserAgentParserInterceptor implements OperationLoggerInterceptor {

    @Override
    public void beforeLog(OperationLogDTO opLog) {
        UserAgent userAgent = UserAgentUtil.parse(opLog.getTerminalInfo());
        opLog.setExtField("userAgent", userAgent);
    }

}
