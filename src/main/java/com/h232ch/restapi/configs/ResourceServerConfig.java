package com.h232ch.restapi.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

@Configuration
@EnableResourceServer
// 사용자 -> 인증요청 -> OAuth 인증 -> 토큰 발급 -> 리소스 권한 확인 (이부분을 여기서 구현)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("event"); // Resource ID만 설정 (나머지는 기본 설정)
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .anonymous()
                    .and()
                .authorizeRequests()
                    .mvcMatchers(HttpMethod.GET, "/api/**")
                        .anonymous()
                    .anyRequest()
                        .authenticated()
                    .and()
                .exceptionHandling()
                    .accessDeniedHandler(new OAuth2AccessDeniedHandler()); // 접근 권한이 없는 경우 OAuth2Access 핸들러를 사용한다.
    }
}
