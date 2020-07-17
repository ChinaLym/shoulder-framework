package org.shoulder.core.i18;

import lombok.extern.shoulder.SLog;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 实现了 spring i18n接口的类，扩展了
 * 默认以 HTTP header 的 Accept-Language 作为标记语言。
 * todo 扩展spring i18？
 * @author lym
 */
@SLog
public class ShoulderReloadableResourceBundleMessageSource extends ReloadableResourceBundleMessageSource {
}
