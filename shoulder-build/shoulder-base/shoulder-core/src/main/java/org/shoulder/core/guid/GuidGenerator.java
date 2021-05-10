package org.shoulder.core.guid;

/**
 * @author lym
 */
public interface GuidGenerator<ID_TYPE> {


    /**
     * 生成一个ID
     *
     * @return guid
     */
    ID_TYPE nextId();

}
