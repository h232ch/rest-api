package com.h232ch.restapi.configs;

import com.h232ch.restapi.accounts.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
public class AutoServerConfig extends AuthorizationServerConfigurerAdapter { // OAuth 인증 서버 설정 (아마 /oauth/token 요청을 받는것같다..)

    @Autowired
    PasswordEncoder passwordEncoder; // Client Secret을 확인하기 위해 주입받는다.
    
    @Autowired
    AuthenticationManager authenticationManager; // 유저 정보를 가지고 있는 빈

    @Autowired
    AccountService accountService;

    @Autowired
    TokenStore tokenStore;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("myApp")
                .authorizedGrantTypes("password", "refresh_token") // Grant Type을 refresh_token으로 지정
                .scopes("read", "write")
                .secret(this.passwordEncoder.encode("pass")) // secret (token의 시크릿값을 패스워드 인코더로 해싱처리)
                .accessTokenValiditySeconds(10 * 60) // 얼마동안 유효한지? 10분
                .refreshTokenValiditySeconds(6 * 10 * 10); // 1 시간

    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)
                .userDetailsService(accountService)
                .tokenStore(tokenStore);
    }
}
