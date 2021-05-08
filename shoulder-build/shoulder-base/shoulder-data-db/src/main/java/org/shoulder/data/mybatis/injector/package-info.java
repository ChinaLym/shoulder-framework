/**
 * 扩展了基类提供的方法
 * 这些类并非可以序列化，只是 mybatis-plus 错误的使用了常量接口，故添加 @SuppressWarnings("serial") 压制
 * 参见 https://zhuanlan.zhihu.com/p/115640766
 */
package org.shoulder.data.mybatis.injector;