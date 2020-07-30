package org.shoulder.core.delay;

import org.springframework.lang.NonNull;

/**
 * 延时任务持有者
 *
 * 使用：注入 bean 调用 put 方法进去即可。
 *
 * @author lym
 */
public interface DelayTaskHolder {

    /**
     * 存储任务
     * @param delayTask 已被封装的延时任务
     */
    void put(@NonNull DelayTask delayTask);

    /**
     * 获取要执行的任务
     *
     * @throws  Exception 取时可能会产生中断等候等异常
     * @return 可执行的任务
     */
    @NonNull
    DelayTask next() throws Exception;

}
