/**
 * 用户在录入信息是录入了‘%’，而在查询时无法精确匹配‘%’，因为这是 mysql 关键字，所以需要对其转义，但该插件会导致 "A%B" 中间模糊失效
 * https://blog.csdn.net/u010044936/article/details/107105437/
 */
package org.shoulder.data.mybatis.interceptor.like;