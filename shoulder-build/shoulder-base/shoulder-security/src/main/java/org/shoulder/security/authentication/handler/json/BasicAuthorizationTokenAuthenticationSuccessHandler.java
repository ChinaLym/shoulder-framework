package org.shoulder.security.authentication.handler.json;

import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.core.constant.ShoulderFramework;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collections;

/**
 * 授权Token环境下认证成功处理器（发 token 基于 HTTP Authorization 以及 Oauth2）
 * 流程：C端输入用户名密码，至NodeJs，附带AppId、AppSecret（用于标识门户信息）
 * 后端接收到请求后，经过表单登录认证处理器，认证通过后，在这里颁发授权token，返回给 NodeJs
 * nodeJs 将认证token保存在 session 中，每次使用 session 中的 token 来调用后端 api
 *
 * @author lym
 * TODO 替换 spring security oauth2 项目相关的类，改为替代品
 * 但考虑到 spring-auth-server 还非常不成熟，功能远不及spring security oauth2；短期不换
 * @see RegisteredClientRepository
 */
public class BasicAuthorizationTokenAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    protected final Logger log = ShoulderLoggers.SHOULDER_SECURITY;

    /**
     * 借用 spring-security-oauth 的 oauth2 存储代码
     */
    protected final ClientDetailsService clientDetailsService;

    /**
     * 借用 spring-security-oauth 的发 token 代码，但不走 oauth2 流程
     */
    private final AuthorizationServerTokenServices authorizationServerTokenServices;

    /**
     * 保存登录记录
     */
    //protected AuthenticationRecordService authenticationRecordService;
    public BasicAuthorizationTokenAuthenticationSuccessHandler(ClientDetailsService clientDetailsService, AuthorizationServerTokenServices authorizationServerTokenServices) {
        this.clientDetailsService = clientDetailsService;
        this.authorizationServerTokenServices = authorizationServerTokenServices;
    }

    /**
     * 认证成功时，Json 格式响应
     * {
     * "code": "0",
     * "msg": "success",
     * "data": {
     * "access_token": "457d702b-659e-400f-8485-12b420b8f686",
     * "token_type": "bearer",
     * "expires_in": 43183,
     * "scope": "resourceIds"
     * }
     * }
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.debug("login SUCCESS, try to create access token.");

        // 获取 clientDetails
        ClientDetails clientDetails = loadClientDetail(request);
        AssertUtils.notNull(clientDetails, CommonErrorCodeEnum.CODING);

        // 自定义的认证模式，非4中模式中的
        TokenRequest tokenRequest = new TokenRequest(Collections.emptyMap(), clientDetails.getClientId(),
                clientDetails.getScope(), ShoulderFramework.NAME);

        OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request(clientDetails);

        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);

        OAuth2AccessToken accessToken = authorizationServerTokenServices.createAccessToken(oAuth2Authentication);

        // 这种方式不需要处理页面请求，必然返回 json
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(JsonUtils.toJson(BaseResult.success(accessToken)));

    }

    /**
     * 根据请求获取客户端信息
     *
     * @param request 请求
     * @return ClientDetails not Null
     */
    @Nonnull
    protected ClientDetails loadClientDetail(HttpServletRequest request) throws IOException, ServletException {
        // clientInfo
        String[] clientInfo = extractClientInfo(request);
        // 返回值长度必须至少为 2
        AssertUtils.isTrue(clientInfo.length >= 2, CommonErrorCodeEnum.CODING);

        String clientId = clientInfo[0];
        String clientSecret = clientInfo[1];
        // 这里可以增加校验时间戳

        /*BaseClientDetails mockDetail = new BaseClientDetails();
        mockDetail.setScope(Collections.singleton("all"));
        mockDetail.setRegisteredRedirectUri(Collections.singleton("http://example.com"));*/

        // 查询 clientId
        ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);
        if (clientDetails == null || !StringUtils.equals(clientDetails.getClientSecret(), clientSecret)) {
            // clientId 对应的配置信息不存在、或者 clientSecret 错误。为了安全返回相同错误，不公布具体细节
            throw new UnapprovedClientAuthenticationException("ClientId or clientSecret incorrect." + clientId);
        }
        return clientDetails;
    }

    /**
     * 将请求头中的 Authorization 字段解码，返回用户名、密码（clientId、clientSecret）
     * 默认使用 Basic 形式
     *
     * @return 长度为 2 的数组（clientId、clientDetail）
     * @throws UnapprovedClientAuthenticationException 未批准的客户端认证（无法拿到 oauth2 的 clientDetail 信息）
     */
    protected String[] extractClientInfo(@Nonnull HttpServletRequest request) throws IOException, ServletException, UnapprovedClientAuthenticationException {
        String authorizationValue = fetchAuthorizationFromHeader(request, "Basic ");

        int delimitIndex = authorizationValue.indexOf(":");
        if (delimitIndex == -1) {
            throw new BadCredentialsException("Invalid Basic AuthorizationValue:" + authorizationValue);
        }
        return new String[]{authorizationValue.substring(0, delimitIndex), authorizationValue.substring(delimitIndex + 1)};
    }


    @Nonnull
    protected final String fetchAuthorizationFromHeader(@Nonnull HttpServletRequest request, @Nonnull String type) {
        // // Tomcat Http11InputBuffer   829-831 行，header key 自动转为小写
        String authorizationInHeader = request.getHeader("authorization");
        if (authorizationInHeader == null) {
            authorizationInHeader = request.getHeader("Authorization");
        }

        if (authorizationInHeader == null) {
            // 请求头中无client信息
            throw new UnapprovedClientAuthenticationException("Missing client info in request headers.");
        }
        if (!authorizationInHeader.startsWith(type)) {
            throw new UnapprovedClientAuthenticationException("Client info in request headers is not valid(not start with 'Basic ')!");
        }

        Charset charset = Charset.forName(request.getCharacterEncoding());
        // 去掉
        byte[] base64Token = authorizationInHeader.substring(type.length()).getBytes(charset);
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Token);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("Failed to decode authentication token with Base64:" + authorizationInHeader);
        }

        return new String(decoded, charset);
    }

}
