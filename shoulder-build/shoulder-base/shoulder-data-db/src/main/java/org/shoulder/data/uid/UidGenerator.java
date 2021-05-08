package org.shoulder.data.uid;

/**
 * guid
 *
 * @author lym
 */
public interface UidGenerator {

    /**
     * 生产下一个 id
     *
     * @param bizType   业务类型
     * @param bizSource id 源字段
     * @return id
     */
    String next(String bizType, String bizSource);
}
