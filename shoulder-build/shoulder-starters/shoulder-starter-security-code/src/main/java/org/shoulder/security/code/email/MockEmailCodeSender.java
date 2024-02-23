package org.shoulder.security.code.email;

/**
 * email发送
 * 这里仅向控制台打印，实际中一般会调用消息推送服务，向目标手机号发送email
 * 如何覆盖该实现？自行实现 EmailCodeSender 接口注入到 spring 中即可。
 *
 * @author lym
 */
public class MockEmailCodeSender implements EmailCodeSender {

    @Override
    public void send(String email, String code) {
        // 实际email这种资源需要限流，如相同手机号（相同业务）每分钟只能发起一次请求
        System.out.println("假装向email " + email + " 发送email验证码 " + code);
    }

}
