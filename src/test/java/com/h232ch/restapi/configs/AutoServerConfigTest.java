package com.h232ch.restapi.configs;


import com.h232ch.restapi.accounts.Account;
import com.h232ch.restapi.accounts.AccountRole;
import com.h232ch.restapi.accounts.AccountService;
import com.h232ch.restapi.common.AppProperties;
import com.h232ch.restapi.common.BaseControllerTest;
import com.h232ch.restapi.common.TestDescription;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AutoServerConfigTest extends BaseControllerTest { // Controller Test를 위해 BaseController를 받는다. (BaseController 내에 애노테이션 등이 지정되어 있음)

    @Autowired
    AccountService accountService;

    @Autowired
    AppProperties appProperties;

    @Test
    @TestDescription("인증 토큰을 발급 받는 테스트")
    public void getAuthToken() throws Exception { // Grant Type (토큰을 받아오는 방법) : password와 refresh Token 두가지 방법을 사용할 것
        // password를 이용한 방식은 Hop(Req, Res 한싸이클) 이 한번이다. (Oauth 서버에 username, password 등 필요한 정보를 보내면 바로 토큰을 받아오는 방식)
        // 패스워드를 사용하기 때문에 써드파티 앱에 절대로 사용하면 안됨. 이 방식은 Username, password를 직접 저장해서 관리하는 경우 사용함


        // Given
//        String username = "sh@naver.com";
//        String password = "sh";
//        Account sh = Account.builder()
//                .email(username)
//                .password(password)
//                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
//                .build();

//        this.accountService.saveAccount(sh);

//        String clientId = "myApp";
//        String clientSecret = "pass";

        this.mockMvc.perform(post("/oauth/token")
                    .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret())) // OAuth 요청 헤더를 만들기 위해 httpBasic을 이용한다 (아래는 OAuth password 타입 인증에 필요한 정보를 헤더에 담아 요청함)
                    .param("username", appProperties.getUserUsername())
                    .param("password", appProperties.getUserPassword())
                    .param("grant_type", "password")) // httpBasic 사용시 spring security test를 추가해야 함 (clientId와 Secret을 이용하여 Basic Auth 헤더를 만들었다)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists());

    }
}