package org.shoulder.security.authentication.handler;

import org.shoulder.core.context.AppInfo;
import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;

/**
 * APP环境下认证成功处理器（基于 Oauth2）
 *
 * @author lym
 */
public class TokenAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 借用 spring-security-oauth 的 oauth2 存储代码
     */
    private ClientDetailsService clientDetailsService;

    /**
     * 借用 spring-security-oauth 的发 token 代码，但不走 oauth2 流程
     */
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    /**
     * 保存登录记录
     */
    //protected AuthenticationRecordService authenticationRecordService;
    public TokenAuthenticationSuccessHandler(ClientDetailsService clientDetailsService, AuthorizationServerTokenServices authorizationServerTokenServices) {
        this.clientDetailsService = clientDetailsService;
        this.authorizationServerTokenServices = authorizationServerTokenServices;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        logger.info("login success");

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Basic ")) {
            // 请求头中无client信息
            throw new UnapprovedClientAuthenticationException("Missing client info in request headers.");
        }

        //解码
        String[] tokens = extractAndDecodeHeader(header, request);
        assert tokens.length == 2;

        String clientId = tokens[0];
        String clientSecret = tokens[1];

        /*BaseClientDetails mockDetail = new BaseClientDetails();
        mockDetail.setScope(Collections.singleton("all"));
        mockDetail.setRegisteredRedirectUri(Collections.singleton("http://example.com"));*/

        // 查询 clientId
        ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);
        if (clientDetails == null || !StringUtils.equals(clientDetails.getClientSecret(), clientSecret)) {
            // clientId 对应的配置信息不存在、或者 clientSecret 错误。为了安全返回相同错误，不公布具体细节
            throw new UnapprovedClientAuthenticationException("ClientId or clientSecret incorrect." + clientId);
        }

        // 自定义的认证模式，非4中模式中的
        TokenRequest tokenRequest = new TokenRequest(Collections.emptyMap(), clientId,
            clientDetails.getScope(), "shoulder");

        OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request(clientDetails);

        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);

        OAuth2AccessToken accessToken = authorizationServerTokenServices.createAccessToken(oAuth2Authentication);

        // 这种方式不需要处理页面请求，必然返回 json
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(JsonUtils.toJson(RestResult.success(accessToken)));

    }

    /**
     * 将请求头中的 Authorization 字段解码，返回用户名、密码（clientId、clientSecret）
     */
    private String[] extractAndDecodeHeader(String header, HttpServletRequest request) throws IOException {

        // 6： 去掉 'Basic '
        byte[] base64Token = header.substring(6).getBytes(AppInfo.charset());
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Token);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("Failed to decode basic authentication token");
        }

        String token = new String(decoded, AppInfo.charset());

        // 分割用户名密码的符号位置
        int delimitIndex = token.indexOf(":");

        if (delimitIndex == -1) {
            throw new BadCredentialsException("Invalid basic authentication token");
        }
        return new String[]{token.substring(0, delimitIndex), token.substring(delimitIndex + 1)};
    }

}
