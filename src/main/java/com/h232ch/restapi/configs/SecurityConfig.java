package com.h232ch.restapi.configs;

import com.h232ch.restapi.accounts.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration // 빈 설정 파일이다.
@EnableWebSecurity // 해당 애노테이션을 붙이는 순간 SpringBoot가 자체적으로 제공하는 Spring Security는 적용되지 않는다.
// 이 파일의 설정에 따라 Spring Security를 적용할 수 있음
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Autowired
    AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Bean
    public TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception { // AutehnticationManager를 Bean으로 받아옴
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception { // bean으로 받아온 AuthenticationManger를 재정의한다.
        auth.userDetailsService(accountService) // 내가만든 UserDetailsService를 가저다 씀
                .passwordEncoder(passwordEncoder); // 내가만든 PasswordEncoder를 가져다 씀
    }

    @Override
    public void configure(WebSecurity web) throws Exception { // Filter를 적용할지 말지를 정함
        web.ignoring().mvcMatchers("/docs/index.html");
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations()); // 스프링에서 제공하는 정적 위치의 Path
    }

//    @Override
//    protected void configure(HttpSecurity http) throws Exception { //위는 필터를 태우지 않는 방법, 이 방법은 필터는 태우데 그 안에서 재필터링 하는 방법
//        // 즉 필터체인 11개?를 모두 태우면서 그 안에서 걸러내는 작업을 한다. 이는 불필요 리소스를 활용하게 되어 위 방법을 추천한다.
//        http.authorizeRequests()
//                .mvcMatchers("/docs/index.html").anonymous()
//                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).anonymous();
//    }


    @Override
    protected void configure(HttpSecurity http) throws Exception { // 로그인 설정의 핵심
        http
                .anonymous()
                    .and()
                .formLogin()// anonymous에게 폼인즈을 적용하겠다.
                    .and()
                .authorizeRequests() // 허용할 메서드는
//                    .mvcMatchers(HttpMethod.GET, "/api/**").anonymous()// Get 요청에 대해 Anonymous는 허용하겠다.
                    .mvcMatchers(HttpMethod.GET, "/api/**").authenticated()
                    .anyRequest().authenticated(); // 나머지는 인증이 필요하다. (로그인이 필요하다는 뜻)
    }
}
