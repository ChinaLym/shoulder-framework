package org.shoulder.core.util;

import cn.hutool.core.map.MapUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 字典树
 * todo 补充完善
 * @deprecated 补充完善前不发布使用
 * @author lym
 */
public class TrieTree<V> {
    private TrieNode<V> root;

    public TrieTree() {
        root = new TrieNode<V>('\u0000', null);
    }

    /**
     * 查询str字符串是否存在，不存在返回null，存在返回该字符串的值
     *
     * @param str
     * @return value
     */
    public V query(String str) {
        TrieNode<V> cur = root;
        for (int i = 0; i < str.length(); i++) {
            TrieNode<V> child = cur.getNodeMap().get(str.charAt(i));
            if (child == null) {
                return null;
            } else {
                cur = child;
            }
        }
        return cur.getValue();
    }

    /**
     * 添加
     */
    public void put(String str, V value) {
        TrieNode<V> cur = root;
        for (int i = 0; i < str.length(); i++) {
            TrieNode<V> child = cur.getNodeMap().get(str.charAt(i));
            if (child == null) {
                TrieNode<V> node = new TrieNode<V>(str.charAt(i), null);
                cur.getNodeMap().put(str.charAt(i), node);
                cur = cur.getNodeMap().get(str.charAt(i));
            } else {
                cur = child;
            }
        }
        cur.setValue(value);
    }

    /**
     * 判断str是否存在
     */
    public boolean exist(String str) {
        TrieNode<V> cur = root;
        for (int i = 0; i < str.length(); i++) {
            TrieNode<V> child = cur.getNodeMap().get(str.charAt(i));
            if (child == null) {
                return false;
            } else {
                cur = child;
            }
        }
        return true;
    }


    /**
     * 删除str字符串
     * 三种情况
     */
    public void remove(String str) {
        if (!exist(str)) {
            return;
        }
        TrieNode<V> cur = root;
        TrieNode<V> delPreNode = root;
        char delch = str.charAt(0);
        for (int i = 0; i < str.length(); i++) {
            TrieNode<V> child = cur.nodeMap.get(str.charAt(i));
            if (MapUtil.isEmpty(child.getNodeMap())) {
                //后面没有结点
                return;
            } else if (i < str.length() - 1 && (child.nodeMap.get(str.charAt(i + 1)) != null) && child.nodeMap.size() > 0) {
                delPreNode = child;
                delch = str.charAt(i + 1);
            }
            cur = child;
        }
        if (cur.nodeMap.size() > 0) {
            cur.setValue(null);
        } else {
            cur.getNodeMap().remove(delch);
        }
    }



    /**
     * 字典树的节点
     *
     * @author lym
     */
    static class TrieNode<VALUE> {
        private char ch;
        private VALUE value;
        private Map<Character, TrieNode<VALUE>> nodeMap;

        public TrieNode(char ch, VALUE value) {
            this.ch = ch;
            this.value = value;
            this.nodeMap = new HashMap<Character, TrieNode<VALUE>>();
        }

        public char getCh() {
            return ch;
        }

        public void setCh(char ch) {
            this.ch = ch;
        }

        public VALUE getValue() {
            return value;
        }

        public void setValue(VALUE value) {
            this.value = value;
        }

        public Map<Character, TrieNode<VALUE>> getNodeMap() {
            return nodeMap;
        }

        public void setNodeMap(Map<Character, TrieNode<VALUE>> nodeMap) {
            this.nodeMap = nodeMap;
        }
    }

}