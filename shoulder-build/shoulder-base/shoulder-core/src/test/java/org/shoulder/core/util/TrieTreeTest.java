package org.shoulder.core.util;

import org.junit.Test;

/**
 * 字典树测试
 *
 * @author lym
 */
public class TrieTreeTest {


    @Test
    public void testAdd() {
        TrieTree<Object> trieTree = new TrieTree<>();
        trieTree.put("abc", null);
        assert trieTree.contains("abc");
        assert trieTree.find("abc").getCh() == 'c';
        trieTree.delete("abc");
        assert !trieTree.contains("abc");

    }

}
