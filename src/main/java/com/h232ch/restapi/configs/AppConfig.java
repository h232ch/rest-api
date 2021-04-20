package com.h232ch.restapi.configs;

import com.h232ch.restapi.accounts.Account;
import com.h232ch.restapi.accounts.AccountRepository;
import com.h232ch.restapi.accounts.AccountRole;
import com.h232ch.restapi.accounts.AccountService;
import com.h232ch.restapi.common.AppProperties;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class AppConfig {

    @Bean // 스프링 IoC에 빈으로 직접 등록해줌 (Bean으로 직접 등록하는 방법)
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder(); // 패스워드 앞에 다양한 알고리즘 프리픽스를 붙여줌
    }

    @Bean
    public ApplicationRunner applicationRunner() { // 러너를 사용해서 실 서비스에 어카운트 한개를 추가해준다 , 이것을 사용해서 로그인 가능~
        return new ApplicationRunner() {

            @Autowired
            AccountService accountService;

            @Autowired
            AppProperties appProperties;

            @Override
            public void run(ApplicationArguments args) throws Exception {

                Account admin = Account.builder()
                        .email(appProperties.getAdminUsername())
                        .password(appProperties.getAdminPassword())
                        .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                        .build();
                accountService.saveAccount(admin);

                Account user = Account.builder()
                        .email(appProperties.getUserUsername())
                        .password(appProperties.getUserPassword())
                        .roles(Set.of(AccountRole.USER))
                        .build();
                accountService.saveAccount(user);

            }
        };
    }
}
