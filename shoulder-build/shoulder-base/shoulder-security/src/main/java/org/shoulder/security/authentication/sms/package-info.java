/**
 * 提供手机号码认证（扩展 spring security 认证方式）
 * 该 api 只输入手机号码就能登录，因此需要与 手机短信验证码过滤器 配合使用，如 shoulder-starter-code 包中提供了 手机短信验证码 的方式对其保护
 * 实际中可以自由添加自定义的方式，如既可以手机号+短信验证码登录、也可以手机号+密码登录，还可以手机号+指纹，手机号+人脸登录
 *
 * @author lym
 */
package org.shoulder.security.authentication.sms;
