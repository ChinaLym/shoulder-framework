package org.shoulder.core.util;

/**
 * 字典树测试
 * 字典树应用场景：
 * 字符串长度是有限的（Trie的深度是可控制的）
 * 字符串检索；海量重复词有限内存去重；前缀匹配、敏感词过滤、文本预测、自动完成、拼写检查；词频统计；字典排序；最长公共前缀
 *
 * @author lym
 */
public class TrieTreeTest {


    //@Test
    public void testAdd() {
        TrieTree<Object> trieTree = new TrieTree<>();
        trieTree.put("abc", null);
        assert trieTree.contains("abc");
        assert trieTree.find("abc").getCh() == 'c';
        trieTree.delete("abc");
        assert !trieTree.contains("abc");

    }

}
