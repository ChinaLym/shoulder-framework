package org.shoulder.core.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 字典树
 * todo 【性能】 substring 会有 new char[] 的操作，递归时可以通过共用 char[] + 下标来实现
 * 【性能】递归替换为循环
 * 【性能|扩展】由于该数据结构大部分应用场景是海量数据，对内存十分敏感、故不作过多扩展性设计
 *
 * @author lym
 * @deprecated 暂不提供
 */
public class TrieTree<V> {


    private TrieNode<V> root;

    public TrieTree() {
        root = new RootNode<>();
    }

    /**
     * 添加
     */
    public void put(String str, V value) {
        TrieNode<V> node = root.getChildren().get(str.charAt(0));
        if (node == null) {
            root.add(str);
        } else {
            node.add(str);
        }
    }

    public void delete(String str) {
        TrieNode<V> node = root.getChildren().get(str.charAt(0));
        if (node != null) {
            node.delete(str);
        }
    }


    /**
     * 判断词条是否在字典树中存在
     * @param words 单词
     * @return 是否存在
     */
    public boolean contains(String words) {
        TrieNode<V> result = find(words);
        return result != null && result.isPresent();
    }

    /**
     * 查找节点
     * @param words 单词
     * @return 结果
     */
    @Nullable
    public TrieNode<V> find(String words) {
        if (StringUtils.isEmpty(words)) {
            return null;
        }
        TrieNode<V> node = root.children.get(words.charAt(0));
        if (node == null) {
            return null;
        }
        return node.find(words);
    }

    /**
     * 字典树的节点
     *
     * @author lym
     */
    static class TrieNode<VALUE> {

        /**
         * 字典值
         */
        protected char ch;

        /**
         * 节点扩展数据，如使用 Integer 计数
         */
        protected VALUE value;

        /**
         * 子节点
         */
        protected Map<Character, TrieNode<VALUE>> children = new ConcurrentHashMap<>();

        /**
         * 从头节点到此节点是否为已有的完整的单词
         */
        protected boolean present = false;

        /**
         * 层级
         */
        //private int level = 0;

        protected final Consumer<TrieNode> DELETE_OPERATION = (node) -> node.present = false;

        protected final BiConsumer<String, VALUE> ADD_DUPLICATE_VALUE = (k, newValue) -> {
            this.value = newValue;
        };

        public TrieNode() {

        }

        public TrieNode(char ch) {
            this.ch = ch;
        }


        public TrieNode(char ch, VALUE value) {
            this.ch = ch;
            this.value = value;
        }

        public TrieNode(char ch, VALUE value, boolean present) {
            this(ch, value);
            this.present = present;
        }

        public TrieNode(char ch, VALUE value, boolean present, Map<Character, TrieNode<VALUE>> children) {
            this(ch, value, present);
            this.children = children;
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

        public Map<Character, TrieNode<VALUE>> getChildren() {
            return children;
        }

        public void setChildren(Map<Character, TrieNode<VALUE>> children) {
            this.children = children;
        }

        /**
         * 为该节点增加下级词条
         *
         * @param words 要添加的词，基于本节点的子串，如 希望添加单词 abcd，当前节点为 b，那 word = cd
         */
        public void add(@Nonnull String words) {
            if (StringUtils.isEmpty(words)) {
                return;
            }
            char firstChar = words.charAt(0);
            //assert words.charAt(0) == ch;
            TrieNode<VALUE> temp = this;
            TrieNode<VALUE> aimChild = temp.children.computeIfAbsent(firstChar,
                key -> new TrieNode<>(key, null, words.length() == 1));
            aimChild.add(words.substring(1));
        }

        /**
         * 为该节点增加下级词条
         *
         * @param words 要删的词，基于本节点的子串，如 希望删除 abcd，当前节点为 b，那 word = bcd
         */
        public void delete(@Nonnull String words) {
            if (words.length() == 1) {
                // 删除本节点
                if (words.charAt(0) == this.ch) {
                    delete();
                }
                return;
            }
            TrieNode<VALUE> temp = this;
            char firstChar = words.charAt(1);
            TrieNode<VALUE> aimChild = temp.children.get(firstChar);
            if (aimChild != null) {
                aimChild.delete(words.substring(1));
            }
        }


        public void delete() {
            DELETE_OPERATION.accept(this);
        }

        /**
         * 查找词条
         *
         * @param words 单词，基于本节点的子串，如 希望搜索 abcd，当前节点为 b，那 word = bcd
         * @return 不存在返回 null
         */
        public TrieNode<VALUE> find(String words) {
            if (StringUtils.isEmpty(words)) {
                return null;
            }
            if (words.length() == 1) {
                // 查询本节点
                return words.charAt(0) == this.ch && isPresent() ? this : null;
            }
            TrieNode<VALUE> temp = this;
            char firstChar = words.charAt(1);
            TrieNode<VALUE> aimChild = temp.children.get(firstChar);
            return aimChild == null ? null : aimChild.find(words.substring(1));
        }

        /**
         * 节点为空
         */
        private boolean hasChildren() {
            return children != null && !children.isEmpty();
        }

        /**
         * 是否存在该值
         *
         * @return 是否存在该值
         */
        private boolean isPresent() {
            return present;
        }
    }


    /**
     * 根节点
     *
     * @param <VALUE> value 类型
     */
    static class RootNode<VALUE> extends TrieNode<VALUE> {

        public RootNode() {
            super('\u0000');
        }

        /**
         * 添加
         */
        public void put(String words, VALUE value) {
            TrieNode<VALUE> node = super.children.get(words.charAt(0));
            if (node == null) {
                super.add(words);
            } else {
                node.add(words);
            }
        }

        /**
         * 查找词条
         * @param words 单词，基于本节点的子串，如 希望搜索 abcd，当前节点为 b，那 word = bcd
         * @return 不存在返回 null
         */
        @Override
        public TrieNode<VALUE> find(String words) {
            if (StringUtils.isEmpty(words)) {
                return null;
            }
            TrieNode<VALUE> node = super.children.get(words.charAt(0));
            if (node == null) {
                return null;
            }
            return node.find(words);
        }


        @Override
        public void delete(@Nullable String words) {
            if (StringUtils.isEmpty(words)) {
                return;
            }
            TrieNode<VALUE> node = super.children.get(words.charAt(0));
            if (node != null) {
                node.delete(words);
            }
        }

        /**
         * 是否存在该值
         * @return 是否存在该值
         */
        private boolean isPresent() {
            throw new IllegalCallerException("root node not support!");
        }

    }


}
